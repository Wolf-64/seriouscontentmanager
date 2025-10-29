package com.wlf.common.themes;

import atlantafx.base.theme.Theme;
import javafx.application.Application;

public class Caspian implements Theme {
    @Override
    public String getName() {
        return "Caspian";
    }

    @Override
    public String getUserAgentStylesheet() {
        return Application.STYLESHEET_CASPIAN;
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
