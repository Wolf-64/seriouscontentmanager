package com.wlf.common.themes;

import atlantafx.base.theme.Theme;

public class ModenaDark implements Theme {
    @Override
    public String getName() {
        return "Modena Dark";
    }

    @Override
    public String getUserAgentStylesheet() {
        return getClass().getResource("modenaDark.css").toString();
    }

    @Override
    public String getUserAgentStylesheetBSS() {
        return null;
    }

    @Override
    public boolean isDarkMode() {
        return true;
    }
}
