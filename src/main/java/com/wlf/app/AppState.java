package com.wlf.app;

import lombok.Getter;
import lombok.Setter;

public class AppState {
    @Getter @Setter
    private boolean languageChanged = false;
    @Getter @Setter
    private boolean initializing = true;
}
