package com.wlf.common.controls;

import com.wlf.common.OverlayStageController;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Separate stage without window decorations to use as overlay pane within the application.<br>
 * Can be used as help pane, or settings pane for example.
 * @param <T> custom controller for the pane, if necessary
 */
public class OverlayStage<T extends OverlayStageController> extends Stage {
    private static final Logger LOGGER = LoggerFactory.getLogger(OverlayStage.class.getSimpleName());

    @Getter
    T controller;
    /** Event handler to check for mouse clicks within anywhere else in the application. Used to close the pane, if clicked somewhere else. */
    private EventHandler<MouseEvent> mouseEventEventHandler;
    /** Listener used to move the overlay pane along its main window when dragged. */
    private InvalidationListener windowPosListener;
    /** Listener used to minimize the overlay pane along its main windows when minimized (they call it 'iconified'). */
    private ChangeListener<Boolean> windowMinimizeListener;
    /** The control the pane is opened from, used as display root. */
    private final Control paneOrigin;
    /** The stage the pane is opened from. Does not establish a node parent-child relationship (non-modal). */
    private Stage parentStage;
    private Scene scene;

    public OverlayStage(URL fxml, Stage parentStage, Control paneOrigin) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(fxml);
            Parent root = fxmlLoader.load();
            controller = fxmlLoader.getController();
            controller.setStage(this);
            controller.setScene(parentStage.getScene());
            this.parentStage = parentStage;

            initStyle(StageStyle.UNDECORATED);
            setAlwaysOnTop(true);
            scene = new Scene(root);
            setScene(scene);

            mouseEventEventHandler = mouseEvent -> {
                if (controller.isCanCloseOnFocusLost()) {
                    hide();
                }
            };

            windowPosListener = (observable) -> calcPanePosition();
            windowMinimizeListener = (observable, oldValue, newValue) -> setIconified(newValue);
        } catch (Exception ex) {
            LOGGER.error("Error while opening overlay pane", ex);
        }

        this.paneOrigin = paneOrigin;
    }

    @Override
    public void showAndWait() {
        if (!isShowing()) {
            // set up mouse click event filter to react on clicking the main window to close the settings
            controller.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEventEventHandler);

            calcPanePosition();
            controller.getScene().getWindow().xProperty().addListener(windowPosListener);
            controller.getScene().getWindow().yProperty().addListener(windowPosListener);
            parentStage.iconifiedProperty().addListener(windowMinimizeListener);

            super.showAndWait();
        }
    }

    @Override
    public void hide() {
        if (isShowing()) {
            // remove handlers once the pane is hidden to avoid unnecessary updates (or NPEs)
            controller.getScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseEventEventHandler);
            controller.getScene().getWindow().xProperty().removeListener(windowPosListener);
            controller.getScene().getWindow().yProperty().removeListener(windowPosListener);
            parentStage.iconifiedProperty().removeListener(windowMinimizeListener);

            super.hide();
        }
    }

    /**
     * Moves the pane to it's 'origin' (the control it was opened from).<br>
     * <br>
     * Usually bottmom-right-aligned, so under the control, inwards into the parent stage. Only works if the origin
     * is also right aligned to its window. E.g. if the button to open the pane is on the left side of the stage,
     * the overlay pane will 'stick out' the main window.
     */
    public void calcPanePosition() {
        // get current bounds of the originating control, will change after moving the main window,
        // but needs to be re-calculated (local to screen) each 'frame'.
        Bounds bounds = paneOrigin.localToScreen(paneOrigin.getBoundsInLocal());
        // magic number is the settings pane width; TODO declare this somewhere in the FXML or get the actual size otherwise
        setX(bounds.getCenterX() - 430.0 + bounds.getWidth() / 2.0);
        setY(bounds.getCenterY() + bounds.getHeight() / 2.0);
    }
}
