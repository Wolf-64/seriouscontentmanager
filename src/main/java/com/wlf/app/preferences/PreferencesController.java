package com.wlf.app.preferences;

import com.wlf.app.App;
import com.wlf.app.AppLoader;
import com.wlf.app.AppStyle;
import com.wlf.app.logging.LogManager;
import com.wlf.common.BaseController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeRegular;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.event.Level;

import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PreferencesController extends BaseController<GeneralConfig> {
    @FXML
    private ChoiceBox<Language> cmbLanguages;
    @FXML
    private ChoiceBox<AppStyle.Theme> cmbThemes;
    @FXML
    private ChoiceBox<LogManager.LogType> cmbLogType;
    @FXML
    private ChoiceBox<Level> cmbLogLevel;
    @FXML
    private ToggleButton btnDarkMode;
    @FXML
    private CheckBox cbxRestoreWindow, cbxStartFullscreen;

    private final FontIcon warningIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
    private final BooleanProperty languageWarningVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty darkModeToggleDisabled = new SimpleBooleanProperty(false);
    private final BooleanProperty logFileControlsVisible = new SimpleBooleanProperty(true);

    private final ObjectProperty<Ikon> iconDarkModeToggle = new SimpleObjectProperty<>(FontAwesomeSolid.SUN);

    private final ObjectProperty<GeneralConfig> config= new SimpleObjectProperty<>(ConfigManager.getInstance().getGeneralConfig());

    public PreferencesController() {
        model.set(ConfigManager.getInstance().getGeneralConfig());
    }

    @FXML
    public void initialize() {
        warningIcon.setStyle("-fx-icon-color: red");
        btnDarkMode.selectedProperty().bindBidirectional(getConfig().darkModeEnabledProperty());
        cbxRestoreWindow.selectedProperty().bindBidirectional(getConfig().restoreWindowProperty());
        cbxStartFullscreen.selectedProperty().bindBidirectional(getConfig().fullScreenProperty());
        cmbLanguages.setItems(FXCollections.observableList(Arrays.stream(Language.values()).toList()));
        cmbLanguages.getSelectionModel().select(getModel().getLanguage());
        cmbLanguages.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    App.STATE.setLanguageChanged(newValue != model.get().getLanguage());
                    languageWarningVisible.setValue(newValue != model.get().getLanguage());
                });

        cmbThemes.setItems(FXCollections.observableList(Arrays.stream(AppStyle.Theme.values()).toList()));
        cmbThemes.getSelectionModel().select(getModel().getActiveTheme());
        cmbThemes.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    if (newValue.getLightTheme() == null) {
                        getModel().setDarkModeEnabled(true);
                        setDarkModeToggleDisabled(true);
                        onToggleDarkMode();
                    } else if (newValue.getDarkTheme() == null) {
                        getModel().setDarkModeEnabled(false);
                        setDarkModeToggleDisabled(true);
                        onToggleDarkMode();
                    } else {
                        App.setAppTheme(newValue, getModel().isDarkModeEnabled());
                    }
                });

        cmbLogType.setItems(FXCollections.observableList(Arrays.stream(LogManager.LogType.values()).toList()));
        cmbLogType.getSelectionModel().select(getModel().getLogType());
        cmbLogType.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    getModel().setLogType(newValue);
                    setLogFileControlsVisible(newValue == LogManager.LogType.FILE);
                });
        setLogFileControlsVisible(getModel().getLogType() == LogManager.LogType.FILE);
        cmbLogLevel.setItems(FXCollections.observableList(Arrays.stream(Level.values()).toList()));
        cmbLogLevel.getSelectionModel().select(getModel().getLogLevel());
        cmbLogLevel.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    getModel().setLogLevel(newValue);
                });
    }

    @Override
    public void afterInit() {
        cmbLogType.setConverter(new StringConverter<>() {
            private final ResourceBundle bundle = getResourceBundle();

            @Override
            public String toString(LogManager.LogType logType) {
                if (logType == null) return "<empty>";
                return bundle.getString("logtype." + logType.name());
            }

            @Override
            public LogManager.LogType fromString(String s) {
                try {
                    return LogManager.LogType.valueOf(s.replace("logtype.", ""));
                } catch (IllegalArgumentException e) {
                    return LogManager.LogType.NONE;
                }
            }
        });
    }

    @FXML
    public void onToggleDarkMode() {
        if (getConfig().isDarkModeEnabled()) {
            iconDarkModeToggle.set(FontAwesomeRegular.MOON);
        } else {
            iconDarkModeToggle.set(FontAwesomeSolid.SUN);
        }
        App.setAppTheme(cmbThemes.getSelectionModel().getSelectedItem(), getConfig().isDarkModeEnabled());
    }

    @FXML
    protected void onSave() throws IOException {
        if (App.STATE.isLanguageChanged()) {
            model.get().setLanguage(cmbLanguages.getValue());
            ConfigManager.save();
            AppLoader.reloadGUIs();
        } else {
            ConfigManager.save();
            onCancel();
        }
    }

    @FXML
    protected void onCancel() {

    }

    // ----------------------------------- FX Boilerplate ---------------------------------------

    public boolean getLanguageWarningVisible() {
        return languageWarningVisible.get();
    }

    public BooleanProperty languageWarningVisibleProperty() {
        return languageWarningVisible;
    }

    public void setLanguageWarningVisible(boolean languageWarningVisible) {
        this.languageWarningVisible.set(languageWarningVisible);
    }

    public GeneralConfig getConfig() {
        return config.get();
    }

    public ObjectProperty<GeneralConfig> configProperty() {
        return config;
    }

    public void setConfig(GeneralConfig config) {
        this.config.set(config);
    }

    public boolean isDarkModeToggleDisabled() {
        return darkModeToggleDisabled.get();
    }

    public BooleanProperty darkModeToggleDisabledProperty() {
        return darkModeToggleDisabled;
    }

    public void setDarkModeToggleDisabled(boolean darkModeToggleDisabled) {
        this.darkModeToggleDisabled.set(darkModeToggleDisabled);
    }

    public boolean isLogFileControlsVisible() {
        return logFileControlsVisible.get();
    }

    public BooleanProperty logFileControlsVisibleProperty() {
        return logFileControlsVisible;
    }

    public void setLogFileControlsVisible(boolean logFileControlsVisible) {
        this.logFileControlsVisible.set(logFileControlsVisible);
    }

    public Ikon getIconDarkModeToggle() {
        return iconDarkModeToggle.get();
    }

    public ObjectProperty<Ikon> iconDarkModeToggleProperty() {
        return iconDarkModeToggle;
    }

    public void setIconDarkModeToggle(Ikon iconDarkModeToggle) {
        this.iconDarkModeToggle.set(iconDarkModeToggle);
    }
}
