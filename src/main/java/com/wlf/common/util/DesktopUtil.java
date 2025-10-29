package com.wlf.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DesktopUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopUtil.class.getSimpleName());

    public static void openBrowser(String url) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI.create(url));
            } catch (Exception e) {
                LOGGER.error("Error opening URL", e);
            }
        }
    }

    public static void openMail(String emailAddress) throws URISyntaxException, IOException {
        Desktop.getDesktop().mail(new URI("mailto:" + emailAddress));
    }

    public static void openFileManager(String path) {
        try {
            File file = new File(path);
            if (file.isDirectory()) {
                Desktop.getDesktop().open(file);
            } else {
                Desktop.getDesktop().open(file.getParentFile());
            }
        } catch (IOException e) {
            LOGGER.error("Error trying to open file manager", e);
        }
    }

    public static void openFile(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            LOGGER.error("Error trying to open file manager", e);
        }
    }
}
