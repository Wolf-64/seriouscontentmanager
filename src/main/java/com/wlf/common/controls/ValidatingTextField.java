package com.wlf.common.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;
import lombok.Setter;

import java.util.function.Function;

public class ValidatingTextField extends TextField {
    private final BooleanProperty valid = new SimpleBooleanProperty();
    @Setter
    private Function<String, Boolean> validator;

    public ValidatingTextField() {
        super();
        valid.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setStyle(null);
            } else {
                setStyle("-fx-text-fill: tomato; -fx-focus-color: tomato");
            }
        });
        textProperty().addListener((observable, oldVal, newVal) -> {
            if (validator != null) {
                if (!validator.apply(newVal)) {
                    setValid(false);
                } else {
                    setValid(true);
                }
            }
        });
    }

    public boolean isValid() {
        return valid.get();
    }

    public BooleanProperty validProperty() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid.set(valid);
    }
}
