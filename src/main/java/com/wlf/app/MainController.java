package com.wlf.app;

import com.wlf.App;
import com.wlf.app.preferences.PreferencesController;
import com.wlf.common.BaseController;
import com.wlf.common.BaseModel;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.MaskerPane;

public class MainController extends BaseController<BaseModel> {
    @FXML
    private BorderPane content, preferences;

    @FXML
    @Override
    protected void initialize() {

    }

    @FXML
    private void showPreferences() {
        App.MAINSTAGE.centerOnScreen();
        if (preferences.getCenter() == null) {
            App.FRAME_CONTROLLER.setLoading(true);
            AppLoader<PreferencesController> loader = new AppLoader<>("app/preferences.fxml",
                    (gui) -> preferences.setCenter(gui));
            loader.setOnSucceeded((_) -> {
                content.setVisible(false);
                preferences.setVisible(true);
                App.FRAME_CONTROLLER.setLoading(false);
            });
            loader.loadAsync();
        } else {
            content.setVisible(false);
            preferences.setVisible(true);
        }
    }

    @FXML
    public void hidePreferences() {
        content.setVisible(true);
        preferences.setVisible(false);
    }
}
