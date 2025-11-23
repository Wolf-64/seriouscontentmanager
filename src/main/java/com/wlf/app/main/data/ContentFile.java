package com.wlf.app.main.data;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

@Slf4j
public class ContentFile extends File {
    public ContentFile(String pathname) {
        super(pathname);
    }

    public ContentFile(Path path) {
        super(path.toAbsolutePath().toString());
    }

    public ContentModel analyzeContent() throws IOException {
        final ContentModel model = new ContentModel();
        model.setDownloadedFile(this);
        model.setDownloadedFileName(this.getName());
        model.setName(getName().substring(0, getName().lastIndexOf('.')));

        model.setSize(Files.size(toPath()));

        inspectArchive(model);

        // if no *MP folders found, probably a TFE map?
        if (model.getGame() == null) {
            model.setGame(Game.TFE);
        }
        if (model.getType() == null) {
            model.setType(Type.MAP);
        }
        if (model.getModes() == null) {
            model.setModes(Mode.SP);
        }

        return model;
    }

    public Path findFirstLevel() throws IOException {
        ArrayList<Path> wlds = new ArrayList<>();
        try (var fs = FileSystems.newFileSystem(toPath(), Collections.emptyMap())) {
            fs.getRootDirectories()
                    .forEach(root -> {
                        try (Stream<Path> stream = Files.walk(root)) {
                            stream.forEach(wld -> {
                                if (wld.toString().endsWith(".vis")) {
                                    wlds.add(Path.of(wld.toString().replace(".vis", ".wld")));
                                }
                            });
                        } catch (IOException e) {
                            log.error("Error reading content file.", e);
                        }
                    });
        } catch (IOException e) {
            log.error("Error reading content file.", e);
            throw e;
        }

        if (wlds.size() == 1) {
            return wlds.getFirst();
        } else {
            return null;
        }
    }

    private void inspectArchive(ContentModel model) throws IOException {
        try (var fs = FileSystems.newFileSystem(toPath(), Collections.emptyMap())) {
            for (Path root : fs.getRootDirectories()) {
                walkRoot(root, model);
            }
        }
    }

    private void walkRoot(Path root, ContentModel model) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path path : (Iterable<Path>) stream::iterator) {
                if (Files.isDirectory(path) && !path.toString().equals("/")) {
                    inspectDirectory(path, model);
                } else if (Files.isRegularFile(path)) {
                    inspectFile(path, model);
                }
            }
        }
    }

    private void inspectDirectory(Path path, ContentModel model) {
        var dirName = path.getName(path.getNameCount() == 0 ? 0 : path.getNameCount() - 1);
        // maps using DLLs for custom game code
        if (dirName.toString().equals("Bin")) {
            model.setDllUsage(true);
        } else if (model.getGame() == null && path.toString().endsWith("MP")) {
            model.setGame(Game.TSE);
        } else if (model.getModes() == null && dirName.toString().equals("Deathmatch")) {
            model.setModes(Mode.DM);
        }
    }

    private void inspectFile(Path path, ContentModel model) {
        var name = path.getFileName();
        if (name == null) return;

        var fileName = name.toString();

        // do we have a descriptor file for mods?
        if (model.getType() == null && fileName.endsWith(".des")) {
            var baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            var basePath = path.getParent().resolve(baseName);

            // does it also have an actual mod folder?
            if (Files.exists(basePath)) {
                model.setType(Type.MOD);
                model.setName(baseName);
            }
        }
    }
}
