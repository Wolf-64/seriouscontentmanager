package com.wlf.common.util;

import com.wlf.app.App;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class FilebrowserUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilebrowserUtils.class.getSimpleName());

    // story last directory during session
    private static String lastDirectory = System.getProperty("user.home");

    public static void browseForFile(Consumer<File> action) {
        browseForFile(lastDirectory, action);
    }

    public static void browseForFile(Consumer<File> action, FileChooser.ExtensionFilter... filter) {
        browseForFile(lastDirectory, action, filter);
    }

    public static void browseForFile(String defaultDir, Consumer<File> action, FileChooser.ExtensionFilter... filter) {
        if (action == null) {
            throw new IllegalArgumentException("Action consumer must be given.");
        }
        FileChooser fileChooser = new FileChooser();
        if (filter != null && filter.length > 0) {
            fileChooser.getExtensionFilters().addAll(filter);
            fileChooser.setSelectedExtensionFilter(filter[0]);
        } else {
            fileChooser.getExtensionFilters().addAll(getDefaultExtensionFilters());
            fileChooser.setSelectedExtensionFilter(getDefaultExtensionFilters().get(0));
        }
        if (defaultDir != null && Files.exists(Path.of(defaultDir))) {
            fileChooser.setInitialDirectory(new File(defaultDir));
        } else if (lastDirectory != null && Files.exists(Path.of(lastDirectory))) {
            fileChooser.setInitialDirectory(new File(lastDirectory));
        }
        File file = fileChooser.showOpenDialog(App.getFocusedWindow());
        if (file == null) {
            return;
        }

        lastDirectory = file.getParent();
        action.accept(file);
    }

    public static void onFileDragOver(DragEvent dragEvent) {
        if ((dragEvent.getDragboard().hasFiles() && dragEvent.getDragboard().getFiles().size() == 1)
                || dragEvent.getDragboard().hasString()) {
            dragEvent.acceptTransferModes(TransferMode.ANY);
        }
        dragEvent.consume();
    }

    public static List<FileChooser.ExtensionFilter> getDefaultExtensionFilters() {
        return List.of(
                new FileChooser.ExtensionFilter("Text files (.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("JSON files (.json)", "*.json"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
    }

    public static List<FileChooser.ExtensionFilter> getAudioFileExtensionFilters() {
        return List.of(
                new FileChooser.ExtensionFilter("WAVE files", "*.wav"),
                new FileChooser.ExtensionFilter("MP3 files", "*.mp3")
        );
    }
}
