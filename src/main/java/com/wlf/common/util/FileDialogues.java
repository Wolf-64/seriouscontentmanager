package com.wlf.common.util;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;

public class FileDialogues {
    public static File chooseDirectory(Path initialPath) {
        return chooseDirectory(initialPath.toFile());
    }

    public static File chooseDirectory(File initialPath) {
        DirectoryChooser chooser = new DirectoryChooser();
        if (initialPath != null) {
            chooser.setInitialDirectory(initialPath);
        }

        return chooser.showDialog(null);
    }

    public static File chooseFile(Path initialPath, FileChooser.ExtensionFilter... filter) {
        return chooseFile(initialPath.toFile(), filter);
    }

    public static File chooseFile(File initialPath, FileChooser.ExtensionFilter... filter) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(filter);
        if (initialPath != null) {
            chooser.setInitialDirectory(initialPath);
        }

        return chooser.showOpenDialog(null);
    }
}
