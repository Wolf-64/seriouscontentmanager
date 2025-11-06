package com.wlf.app.main.data;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class GroFile extends ContentFile {

    public GroFile(String pathname) {
        super(pathname);
    }


    @Override
    public ContentEntity analyzeContent() {
        final ContentEntity model = new ContentEntity();
        model.setDownloadedFile(this);
        model.setDownloadedFileName(this.getName());
        model.setName(getName().substring(0, getName().lastIndexOf('.')));

        boolean foundLevels;
        // definite TSE content
        boolean foundMPDirs;
        // minimum valid archive info
        boolean foundWld;
        boolean foundBinDir;
        try {
            model.setSize(Files.size(toPath()));
            try (var fs = FileSystems.newFileSystem(toPath(), Collections.emptyMap())) {
                        fs.getRootDirectories()
                        .forEach(root -> {
                            // in a full implementation, you'd have to
                            // handle directories
                            try {
                                Files.walk(root).forEach(path -> {
                                    try {
                                        // check what we need to see what game or if mod or map
                                        if (model.getGame() == null && path.toString().endsWith("MP")) {
                                            model.setGame(Game.TSE);
                                        }
                                        if (model.getModes() == null && path.getFileName().toString().equals("Deathmatch")) {
                                            model.setModes(Mode.DM);
                                        }
                                        // should never happen as mods shouldn't be in gros
                                        if (model.getType() == null && path.getFileName().toString().endsWith(".des")) {
                                            String fileName = path.getFileName().toString();
                                            if (Files.exists(Path.of(fileName.substring(0, fileName.lastIndexOf('.'))))) {
                                                model.setType(Type.MOD);
                                                model.setName(fileName.substring(0, fileName.lastIndexOf('.')));
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
}
