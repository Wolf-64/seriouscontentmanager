package com.wlf.common.themes;

import atlantafx.base.theme.Theme;

public class DelegateTheme extends BaseTheme {
    public DelegateTheme(Theme delegate) {
        this.atlantafxTheme = delegate;
    }

    @Override public String getName() { return atlantafxTheme.getName(); }

    @Override public String getUserAgentStylesheet() { return atlantafxTheme.getUserAgentStylesheet(); }

    @Override public String getUserAgentStylesheetBSS() { return atlantafxTheme.getUserAgentStylesheetBSS(); }

    @Override public boolean isDarkMode() { return atlantafxTheme.isDarkMode(); }

    @Override public String getSceneStyleSheet() { return null; }
}
