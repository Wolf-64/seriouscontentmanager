package com.wlf.common.themes;

public class NordDark extends BaseTheme {

    public NordDark() {
        this.atlantafxTheme = new atlantafx.base.theme.NordDark();
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
