package com.wlf.common.themes;

public class Dracula extends BaseTheme {

    public Dracula() {
        this.atlantafxTheme = new atlantafx.base.theme.Dracula();
    }

    @Override
    public String getName() {
        return atlantafxTheme.getName();
    }

    @Override
    public String getUserAgentStylesheet() {
        return atlantafxTheme.getUserAgentStylesheet();
    }

    @Override
    public String getUserAgentStylesheetBSS() {
        return atlantafxTheme.getUserAgentStylesheetBSS();
    }

    @Override
    public boolean isDarkMode() {
        return atlantafxTheme.isDarkMode();
    }

    @Override
    public String getSceneStyleSheet() {
        return null;
    }
}
