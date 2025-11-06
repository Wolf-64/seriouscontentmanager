package com.wlf.app.main.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

public class ContentFile extends File {
    public ContentFile(String pathname) {
        super(pathname);
    }

    public ContentModel analyzeContent() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Path findFirstLevel() {
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
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (wlds.size() == 1) {
            return wlds.getFirst();
        } else {
            return null;
        }
    }
}
