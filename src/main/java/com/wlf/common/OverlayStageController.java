package com.wlf.common;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverlayStageController extends BaseController {
    protected static final Logger LOGGER = LoggerFactory.getLogger(OverlayStageController.class.getSimpleName());

    @Setter @Getter
    protected Stage stage;
    @Setter @Getter
    protected Scene scene;
    @Getter
    protected boolean canCloseOnFocusLost = true;

    @FXML
    public void initialize() {

    }

    @FXML
    protected void onCancel() {
        stage.close();
    }

}
