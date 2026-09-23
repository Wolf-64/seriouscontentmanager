package com.wlf.common.util;

import com.wlf.app.App;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DirectoryUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryUtils.class.getSimpleName());

    // story last directory during session
    private static String lastDirectory = System.getProperty("user.home");

    public static void browseForDirectory(Consumer<File> action) {
        browseForDirectory(lastDirectory, action);
    }

    public static void browseForDirectory(String defaultDir, Consumer<File> action) {
        if (action == null) {
            throw new IllegalArgumentException("Action consumer must be given.");
        }
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (defaultDir != null && Files.exists(Path.of(defaultDir))) {
            directoryChooser.setInitialDirectory(new File(defaultDir));
        }
        File branchDirectory = directoryChooser.showDialog(App.getFocusedWindow());
        if (branchDirectory == null) {
            return;
        }

        lastDirectory = branchDirectory.getAbsolutePath();
        action.accept(branchDirectory);
    }

    public static void onDirectoryDragOver(DragEvent dragEvent) {
        if ((dragEvent.getDragboard().hasFiles() && dragEvent.getDragboard().getFiles().size() == 1)
                || dragEvent.getDragboard().hasString()) {
            dragEvent.acceptTransferModes(TransferMode.ANY);
        }
        dragEvent.consume();
    }
}
