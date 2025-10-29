package com.wlf.common.themes;

import atlantafx.base.theme.Theme;
import javafx.application.Application;

public class Modena implements Theme {
    @Override
    public String getName() {
        return "Modena";
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
}
