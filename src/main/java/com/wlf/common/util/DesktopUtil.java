package com.wlf.common.util;

import com.wlf.app.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class DesktopUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopUtil.class.getSimpleName());

    public static void openBrowser(String url) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI.create(url));
            } catch (Exception e) {
                App.showErrorMessage(e);
                LOGGER.error("Error opening URL", e);
            }
        }
    }

    public static void openMail(String emailAddress) throws URISyntaxException, IOException {
        Desktop.getDesktop().mail(new URI("mailto:" + emailAddress));
    }

    public static void openInFileManager(Path path) {
        openInFileManager(path.toFile());
    }

    public static void openInFileManager(String path) {
        openInFileManager(new File(path));
    }

    public static void openInFileManager(File file) {
        try {
            if (file.isDirectory()) {
                Desktop.getDesktop().open(file);
            } else {
                Desktop.getDesktop().open(file.getParentFile());
            }
        } catch (IOException e) {
            App.showErrorMessage(e);
            LOGGER.error("Error trying to open file manager", e);
        }
    }

    public static void openFile(String path) {
        openFile(new File(path));
    }

    public static void openFile(Path path) {
        openFile(path.toFile());
    }

    public static void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            App.showErrorMessage(e);
            LOGGER.error("Error trying to open file manager", e);
        }
    }
}
