package com.wlf.app;

import javafx.scene.Node;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class AppState {
    @Getter @Setter
    private boolean languageChanged = false;
    @Getter @Setter
    private boolean initializing = true;
    private final Map<Node, Boolean> visibilities = new HashMap<>();

    public void setVisibility(Node node, boolean value) {
        node.setVisible(value);
    }
}
