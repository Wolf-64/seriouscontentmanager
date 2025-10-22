package com.wlf.common;

import com.wlf.app.Config;
import com.wlf.common.controls.OverlayStage;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class BaseController {
    private final ObjectProperty<Config> configuration = new SimpleObjectProperty<>(Config.getInstance());

    @Getter @Setter
    private List<Consumer<Event>> onBackCallbacks = new ArrayList<>();

    @Setter
    private Runnable onBack;

    @Getter @Setter
    protected Stage stage;
    @Getter @Setter
    protected Scene scene;

    @Getter @Setter
    private List<OverlayStage<?>> overlays = new ArrayList<>();

    /**
     * Method to use to get back to the launcher window from any tools started from it.
     * @param event event for tools to handle consume conditions. Consumed events will prevent the tool from being able
     *              to switch back to the launcher, e.g. in case of running critical tasks that cannot be aborted.
     */
    @FXML
    public final void backToMain(ActionEvent event) {
        for (Consumer<Event> onBackCallback : onBackCallbacks) {
            onBackCallback.accept(event);
        }

        if (!event.isConsumed()) {
            onBack.run();
        }
    }

    /**
     * Called by MainController after FXMLLoader has finished initializing the GUI.
     * Currently used to execute any code that depends on values that are passed to the controller
     *  after initialization, which are not available during @FXML initialize()
     */
    public void afterInit() {
        getStage().setOnCloseRequest(this::closeRequest);
    }

    protected void closeRequest(WindowEvent windowEvent) {
        for (OverlayStage<?> overlayStage : overlays) {
            if (overlayStage.isShowing()) {
                overlayStage.close();
            }
        }
    }
}
