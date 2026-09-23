package com.wlf.common.util;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;

import java.nio.file.Path;
import java.util.function.Supplier;

public class GuiUtils {
    public static void onTextFieldDrop(DragEvent dragEvent) {
        if (dragEvent.getSource() instanceof TextField source) {
            if (dragEvent.getDragboard().hasFiles()) {
                source.setText(dragEvent.getDragboard().getFiles().get(0).toString());
                source.fireEvent(new ActionEvent()); // triggers onAction event
                dragEvent.setDropCompleted(true);
            }
            if (dragEvent.getDragboard().hasString()) {
                source.setText(dragEvent.getDragboard().getString());
                source.fireEvent(new ActionEvent()); // triggers onAction event
                dragEvent.setDropCompleted(true);
            }
        }
        dragEvent.consume();
    }

    /**
     *
     */
    public static ContextMenu createFileContextMenu(Supplier<Path> filePathGetter) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem miCopy = new MenuItem("Copy path");
        MenuItem miOpen = new MenuItem("Open file");
        MenuItem miBrowse = new MenuItem("Open in file manager");

        miCopy.setOnAction((event) -> {
            if (filePathGetter.get() != null) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(filePathGetter.get().toString());
                clipboard.setContent(content);
            }
        });
        miOpen.setOnAction((event) -> {
            if (filePathGetter.get() != null
                    && filePathGetter.get().toFile().exists()
                    && filePathGetter.get().toFile().isFile()) {
                DesktopUtil.openFile(filePathGetter.get());
            } else if (filePathGetter.get() != null
                    && filePathGetter.get().toFile().exists()
                    && filePathGetter.get().toFile().isDirectory()) {
                DesktopUtil.openInFileManager(filePathGetter.get());
            }
        });
        miBrowse.setOnAction((event) -> {
            if (filePathGetter.get() != null) {
                DesktopUtil.openInFileManager(filePathGetter.get());
            }
        });

        contextMenu.getItems().addAll(miCopy, miOpen, miBrowse);
        return contextMenu;
    }
}
