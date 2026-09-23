package com.wlf.common;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;

public interface ProgressAware {
    DoubleProperty progressProperty();
    StringProperty statusProperty();
}
