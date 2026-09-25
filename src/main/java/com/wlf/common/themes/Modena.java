package com.wlf.common.themes;

import com.wlf.common.util.Utils;
import javafx.application.Application;

public class Modena extends BaseTheme {
    @Override
    public String getName() {
        return "Modena (JavaFX default)";
    }

    @Override
    public String getUserAgentStylesheet() {
        return Application.STYLESHEET_MODENA;
    }

    @Override
    public String getUserAgentStylesheetBSS() {
        return null;
    }

    @Override
    public boolean isDarkMode() {
        return false;
    }

    @Override
    public String getSceneStyleSheet() {
        return null;
    }
}
