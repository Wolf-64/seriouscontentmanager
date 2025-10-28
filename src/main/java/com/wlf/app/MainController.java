package com.wlf.app;

import com.wlf.App;
import com.wlf.common.BaseController;
import com.wlf.common.util.AsyncFXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.MaskerPane;

public class MainController extends BaseController {
    @FXML
    private BorderPane content, preferences;
    @FXML
    private MaskerPane loadingMask;

    @FXML
    @Override
    protected void initialize() {

    }

    @FXML
    private void showPreferences() {
        App.MAINSTAGE.centerOnScreen();
        if (preferences.getCenter() == null) {
            setLoading(true);
            AsyncFXMLLoader loader = new AsyncFXMLLoader("app/preferences.fxml");
            loader.setOnSucceeded((preferencesPane) -> {
                content.setVisible(false);
                preferences.setCenter(preferencesPane.getGui());
                preferences.setVisible(true);
                setLoading(false);
            });
            loader.load();
        } else {
            content.setVisible(false);
            preferences.setVisible(true);
        }
    }

    public void setLoading(boolean value) {
        loadingMask.setVisible(value);
    }

    @FXML
    public void hidePreferences() {
        content.setVisible(true);
        preferences.setVisible(false);
    }
}
