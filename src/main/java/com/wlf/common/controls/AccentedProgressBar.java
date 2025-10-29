package com.wlf.common.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ProgressBar;

/**
 * Regular progress bar that will be automatically tinted green whenever it's progress is 100% (1.0),
 * or red if its progress is set externally to {@link #FAILED_PROGRESS}.
 */
public class AccentedProgressBar extends ProgressBar {
    /** Used to indicate failure of a task, coloring the progress bar red. */
    // The only simple way to signal a failure without using extra property that cannot be bound via FXML
    // is to just use any value higher than 1.0 for the bar's progress to set a visual state.
    // Not pretty, but gets the job done pretty effectively.
    public static final double FAILED_PROGRESS = 42; // TODO be less cute...?
    public static final double WARNING_PROGRESS = 86; // TODO be less cute...?
    public static final double SUCCESSFUL_PROGRESS = 8080; // TODO be less cute...?

    /** OBSOLETE. Maybe finds some use later on, but with the current binding situation, it's easier to use special
     * progress numbers instead (see {@link #FAILED_PROGRESS}). */
    private final BooleanProperty errorOccurred = new SimpleBooleanProperty();

    public AccentedProgressBar() {
        super();
        progressProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.intValue() == SUCCESSFUL_PROGRESS) {
                setStyle("-fx-accent: limegreen;");
            } else if (newValue.intValue() == FAILED_PROGRESS) {
                setStyle("-fx-accent: tomato;");
            } else if (newValue.intValue() == WARNING_PROGRESS) {
                setStyle("-fx-accent: gold;");
            } else {
                resetStyle();
            }
        }));

        errorOccurredProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                setStyle("-fx-accent: tomato;");
            } else {
                resetStyle();
            }
        }));
    }

    private void resetStyle() {
        setStyle("");
    }

    public boolean getErrorOccurred() {
        return errorOccurred.get();
    }

    public BooleanProperty errorOccurredProperty() {
        return errorOccurred;
    }

    public void setErrorOccurred(boolean errorOccurred) {
        this.errorOccurred.set(errorOccurred);
    }
}
