package com.wlf.app.preferences;

import com.wlf.App;
import com.wlf.app.AppLoader;
import com.wlf.app.AppStyle;
import com.wlf.common.BaseController;
import com.wlf.common.BaseModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.Arrays;

public class PreferencesController extends BaseController<BaseModel> {
    @FXML
    private ComboBox<Language> cmbLanguages;
    @FXML
    private ComboBox<AppStyle.Theme> cmbThemes;
    private final FontIcon warningIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
    private final BooleanProperty languageWarningVisible = new SimpleBooleanProperty(false);

    @FXML
    public void initialize() {
        warningIcon.setStyle("-fx-icon-color: red");
        cmbLanguages.setItems(FXCollections.observableList(Arrays.stream(Language.values()).toList()));
        cmbLanguages.getSelectionModel().select(getConfiguration().getLanguage());
        cmbLanguages.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    App.STATE.setLanguageChanged(newValue != getConfiguration().getLanguage());
                    languageWarningVisible.setValue(newValue != getConfiguration().getLanguage());
                });

        cmbThemes.setItems(FXCollections.observableList(Arrays.stream(AppStyle.Theme.values()).toList()));
        cmbThemes.getSelectionModel().select(getConfiguration().getActiveTheme());
        cmbThemes.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> App.setTheme(newValue));
    }

    @FXML
    private void onSave() throws IOException {
        Config.save();
        if (App.STATE.isLanguageChanged()) {
            getConfiguration().setLanguage(cmbLanguages.getValue());
            AppLoader.reloadGuis();
        } else {
            App.MAIN_CONTROLLER.hidePreferences();
        }
    }

    @FXML
    private void onCancel() throws IOException {
        App.MAIN_CONTROLLER.hidePreferences();
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
}
