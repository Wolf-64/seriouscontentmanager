package com.wlf.app.main.data;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

public class GroFile extends ContentFile {

    public GroFile(String pathname) {
        super(pathname);
    }
}
