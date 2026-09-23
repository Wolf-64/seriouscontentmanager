package com.wlf.app.preferences;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wlf.app.AppStyle;
import com.wlf.common.BaseModel;
import javafx.beans.property.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Global configuration object containing general settings and tool-specific configurations.
 * Is loaded from and stored to JSON for simplicity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneralConfig extends BaseModel {
    private final ObjectProperty<Language> language = new SimpleObjectProperty<>(Language.ENGLISH);
    /** Active theme index from the global list of themes.*/
    private final ObjectProperty<AppStyle.Theme> activeTheme = new SimpleObjectProperty<>(AppStyle.Theme.NORD);
    private final BooleanProperty darkModeEnabled = new SimpleBooleanProperty();

    /** Width of the main window to save on resize and restore when restarting. */
    private final DoubleProperty windowWidth = new SimpleDoubleProperty();
    /** Height of the main window to save on resize and restore when restarting. */
    private final DoubleProperty windowHeight = new SimpleDoubleProperty();
    /** Stores whether the main window should be maximized */
    private final BooleanProperty fullScreen = new SimpleBooleanProperty(false);
    /** Whether the window sizing should be restored from last session */
    private final BooleanProperty restoreWindow = new SimpleBooleanProperty(false);

    private final BooleanProperty logToFile = new SimpleBooleanProperty(true);
    private final StringProperty logFileTarget = new SimpleStringProperty("app.log");
    private final StringProperty logLevel = new SimpleStringProperty("INFO");


    // not used in UI and only for dev
    @Getter @Setter
    private boolean devMode;

    // --- JavaFX boilerplate that's not covered by Lombok ---
    public AppStyle.Theme getActiveTheme() {
        return activeTheme.get();
    }

    public ObjectProperty<AppStyle.Theme> activeThemeProperty() {
        return activeTheme;
    }

    public void setActiveTheme(AppStyle.Theme activeTheme) {
        this.activeTheme.set(activeTheme);
    }

    public Language getLanguage() {
        return language.get();
    }

    public ObjectProperty<Language> languageProperty() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language.set(language);
    }

    public boolean isDarkModeEnabled() {
        return darkModeEnabled.get();
    }

    public BooleanProperty darkModeEnabledProperty() {
        return darkModeEnabled;
    }

    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.darkModeEnabled.set(darkModeEnabled);
    }

    public boolean isLogToFile() {
        return logToFile.get();
    }

    public BooleanProperty logToFileProperty() {
        return logToFile;
    }

    public void setLogToFile(boolean logToFile) {
        this.logToFile.set(logToFile);
    }

    public String getLogFileTarget() {
        return logFileTarget.get();
    }

    public StringProperty logFileTargetProperty() {
        return logFileTarget;
    }

    public void setLogFileTarget(String logFileTarget) {
        this.logFileTarget.set(logFileTarget);
    }

    public String getLogLevel() {
        return logLevel.get();
    }

    public StringProperty logLevelProperty() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel.set(logLevel);
    }

    public double getWindowWidth() {
        return windowWidth.get();
    }

    public DoubleProperty windowWidthProperty() {
        return windowWidth;
    }

    public void setWindowWidth(double windowWidth) {
        this.windowWidth.set(windowWidth);
    }

    public double getWindowHeight() {
        return windowHeight.get();
    }

    public DoubleProperty windowHeightProperty() {
        return windowHeight;
    }

    public void setWindowHeight(double windowHeight) {
        this.windowHeight.set(windowHeight);
    }

    public boolean isFullScreen() {
        return fullScreen.get();
    }

    public BooleanProperty fullScreenProperty() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen.set(fullScreen);
    }

    public boolean isRestoreWindow() {
        return restoreWindow.get();
    }

    public BooleanProperty restoreWindowProperty() {
        return restoreWindow;
    }

    public void setRestoreWindow(boolean restoreWindow) {
        this.restoreWindow.set(restoreWindow);
    }
}

