package com.wlf.app.main.data;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class ZipFile extends ContentFile {
    public ZipFile(String pathname) {
        super(pathname);
    }

    @Override
    public ContentModel analyzeContent() {
        final ContentModel model = new ContentModel();
        model.setDownloadedFile(this);
        model.setDownloadedFileName(this.getName());
        try {
            model.setSize(Files.size(toPath()));
            FileSystems.newFileSystem(toPath(), Collections.emptyMap())
                    .getRootDirectories()
                    .forEach(root -> {
                        // in a full implementation, you'd have to
                        // handle directories
                        try {
                            Files.walk(root).forEach(path -> {
                                try {
                                    if (model.getType() == null && path.getFileName().toString().endsWith(".des")) {
                                        String fileName = path.getFileName().toString();
                                        // check if a folder of the same name as the .des filee exists
                                        model.setType(Type.MOD);
                                        model.setName(fileName.substring(0, fileName.lastIndexOf('.')));
                                    }

                                    // check what we need to see what game or if mod or map
                                    if (model.getGame() == null && path.toString().endsWith("MP")) {
                                        model.setGame(Game.TSE);
                                    }
                                    if (model.getName() == null && (path.toString().endsWith(".wld")
                                            || path.toString().endsWith(".des"))) {
                                        String fileName = path.getFileName().toString();
                                        model.setName(fileName.substring(0, fileName.lastIndexOf('.')));
                                        // check if a folder of the same name as the .des filee exists
                                        if (Files.exists(Path.of(fileName.substring(0, fileName.lastIndexOf('.'))))) {
                                            model.setType(Type.MOD);
                                        }
                                    }
                                    if (model.getModes() == null && path.getFileName().toString().equals("Deathmatch")) {
                                        model.setModes(Mode.DM);
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

    public void unpack(String destination) {

    }
}
