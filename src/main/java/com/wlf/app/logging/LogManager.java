package com.wlf.app.logging;

import org.apache.logging.log4j.core.config.Configurator;

/**
 * Utility to select which logging setup declared in {@code log4j2.xml} should be active.
 * By default, logging goes to stdout. Callers can switch to a file, or to both, at runtime.
 */
public final class LogManager {

    public enum LogType {
        NONE,
        STDOUT,
        FILE,
        STDOUT_AND_FILE
    }

    // System property names, must match the placeholders referenced in log4j2.xml
    public static final String TARGET_PROPERTY = "log.target";
    public static final String FILE_PROPERTY = "log.file";
    public static final String LEVEL_PROPERTY = "log.level";

    private LogManager() {
    }

    /**
     * Activate one of the logging setups declared in {@code log4j2.xml}.
     *
     * @param destination which declared setup to activate; {@code null} defaults to stdout
     * @param filePath    log file path, required for {@link LogType#FILE} and {@link LogType#STDOUT_AND_FILE}
     * @param logLevel    root level (e.g. INFO, DEBUG); {@code null}/blank keeps the XML default
     * @throws IllegalArgumentException when a file destination is requested without a file path
     */
    public static void configure(LogType destination, String filePath, String logLevel) {
        if (destination == null) {
            System.setProperty(LEVEL_PROPERTY, "OFF");
        }
        boolean toFile = destination == LogType.FILE || destination == LogType.STDOUT_AND_FILE;
        if (toFile && (filePath == null || filePath.isBlank())) {
            throw new IllegalArgumentException("No log file path was provided.");
        }

        System.setProperty(TARGET_PROPERTY, targetValue(destination));
        if (filePath != null && !filePath.isBlank()) {
            System.setProperty(FILE_PROPERTY, filePath);
        }
        if (logLevel != null && !logLevel.isBlank()) {
            System.setProperty(LEVEL_PROPERTY, logLevel);
        }

        // Reload log4j2.xml so its Arbiters and lookups re-evaluate with the properties set above.
        Configurator.reconfigure();
    }

    private static String targetValue(LogType destination) {
        return switch (destination) {
            case NONE -> "none";
            case FILE -> "file";
            case STDOUT_AND_FILE -> "both";
            case STDOUT -> "console";
        };
    }
}
