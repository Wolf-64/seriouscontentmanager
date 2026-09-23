package com.wlf.app.preferences;

import com.wlf.app.App;
import com.wlf.app.AppLoader;
import com.wlf.app.AppStyle;
import com.wlf.common.BaseController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeRegular;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.Arrays;

public class PreferencesController extends BaseController<GeneralConfig> {
    @FXML
    private ComboBox<Language> cmbLanguages;
    @FXML
    private ComboBox<AppStyle.Theme> cmbThemes;
    @FXML
    private ToggleButton btnDarkMode;
    @FXML
    private CheckBox cbxRestoreWindow, cbxStartFullscreen;

    private final FontIcon warningIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
    private final BooleanProperty languageWarningVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty darkModeToggleDisabled = new SimpleBooleanProperty(false);

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
        cmbLanguages.getSelectionModel().select(model.get().getLanguage());
        cmbLanguages.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    App.STATE.setLanguageChanged(newValue != model.get().getLanguage());
                    languageWarningVisible.setValue(newValue != model.get().getLanguage());
                });

        cmbThemes.setItems(FXCollections.observableList(Arrays.stream(AppStyle.Theme.values()).toList()));
        cmbThemes.getSelectionModel().select(model.get().getActiveTheme());
        cmbThemes.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    if (newValue.getLightTheme() == null) {
                        getConfig().setDarkModeEnabled(true);
                        setDarkModeToggleDisabled(true);
                        onToggleDarkMode();
                    } else if (newValue.getDarkTheme() == null) {
                        getConfig().setDarkModeEnabled(false);
                        setDarkModeToggleDisabled(true);
                        onToggleDarkMode();
                    } else {
                        App.setAppTheme(newValue, getConfig().isDarkModeEnabled());
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
}
