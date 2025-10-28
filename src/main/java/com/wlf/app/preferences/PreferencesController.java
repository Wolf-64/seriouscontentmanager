package com.wlf.app.preferences;

import com.wlf.App;
import com.wlf.app.AppStyle;
import com.wlf.app.Config;
import com.wlf.common.OverlayStageController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

import java.io.IOException;
import java.util.Arrays;

public class PreferencesController extends OverlayStageController {
    @FXML
    private ComboBox<Language> cmbLanguages;
    @FXML
    private ComboBox<AppStyle.Theme> cmbThemes;

    @FXML
    public void initialize() {
        cmbLanguages.setItems(FXCollections.observableList(Arrays.stream(Language.values()).toList()));
        cmbLanguages.getSelectionModel().select(getConfiguration().getLanguage());
        cmbLanguages.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> App.setLanguage(newValue));

        cmbThemes.setItems(FXCollections.observableList(Arrays.stream(AppStyle.Theme.values()).toList()));
        cmbThemes.getSelectionModel().select(getConfiguration().getActiveTheme());
        cmbThemes.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> App.setTheme(newValue));
    }

    @FXML
    public void onSave() throws IOException {
        Config.save();
        App.FRAME_CONTROLLER.hidePreferences();
    }
}
