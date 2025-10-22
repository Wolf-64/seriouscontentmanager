package com.wlf.common.util;

import com.wlf.App;
import javafx.scene.image.Image;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Objects;

public class Utils {
    public static Image getImageResource(String path) {
        return new Image(Objects.requireNonNull(App.class.getResource(path)).toExternalForm());
    }

    public static String getCss(String path) {
        return Objects.requireNonNull(App.class.getResource(path)).toExternalForm();
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static java.lang.String sanitizePath(String path) {
        if (path.contains("\\") && !path.endsWith("\\")) {
            path += "\\";
        }

        if (path.contains("/") && !path.endsWith("/")) {
            path += "/";
        }

        return path;
    }

    public static String millisToHHMMSS(Long millis) {
        long sec = millis == null ? 0 : millis / 1000;
        long min = sec / 60;
        long hours = min / 60;
        return String.format("%02d:%02d:%02d", hours, min % 60, sec % 60);
    }

    public static String millisToMMSS(Long millis) {
        long sec = millis == null ? 0 : millis / 1000;
        long min = sec / 60;
        return String.format("%02d:%02d", min, sec % 60);
    }

    public static String createExternalPathString(String path) {
        try {
            return new File(path).toURI().toURL().toExternalForm();
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
