package com.wlf.app.main.io;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.ProviderNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EncodingFileSystems {

    private static final List<String> encodings = List.of(
            "Cp437",
            "windows-1252",
            "Cp866"
    );

    public static FileSystem newFileSystem(Path path) {
        Map<String, String> env = new HashMap<>();

        FileSystem fs = null;
        int encodingIndex = 0;
        do {
            try {
                fs = FileSystems.newFileSystem(path, env);
            } catch (ProviderNotFoundException | IOException e) {
                env.clear();
                env.put("encoding", encodings.get(encodingIndex));
                encodingIndex++;
            }
        } while (fs == null || encodingIndex == encodings.size());

        return fs;
    }
}
