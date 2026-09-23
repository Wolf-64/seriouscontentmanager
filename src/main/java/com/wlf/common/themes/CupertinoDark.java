package com.wlf.common.themes;

public class CupertinoDark extends BaseTheme {

    public CupertinoDark() {
        this.atlantafxTheme = new atlantafx.base.theme.CupertinoDark();
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
