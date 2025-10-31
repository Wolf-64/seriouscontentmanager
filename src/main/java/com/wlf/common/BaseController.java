package com.wlf.common;

import com.wlf.app.preferences.Config;
import com.wlf.common.controls.OverlayStage;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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

public abstract class BaseController<T extends BaseModel> {
    protected final ObjectProperty<Config> configuration = new SimpleObjectProperty<>(Config.getInstance());
    protected ObjectProperty<T> model = new SimpleObjectProperty<>();

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

    @FXML
    protected abstract void initialize();

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

    // ---------------------------------- FX Boilerplate ---------------------------------

    public Config getConfiguration() {
        return configuration.get();
    }

    public ObjectProperty<Config> configurationProperty() {
        return configuration;
    }

    public T getModel() {
        return model.get();
    }

    public ObjectProperty<T> modelProperty() {
        return model;
    }

    public void setModel(T model) {
        this.model.set(model);
    }
}
