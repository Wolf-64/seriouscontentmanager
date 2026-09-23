package com.wlf.common.themes;

import atlantafx.base.theme.Theme;

public abstract class BaseTheme implements Theme {
    protected Theme atlantafxTheme;

    public abstract String getSceneStyleSheet();

    /**
     * @return true if this theme is backed by an AtlantaFX user-agent stylesheet, false for built-in
     * JavaFX themes (Modena, Caspian). AtlantaFX themes require the GemsFX/ControlsFX AtlantaFX
     * integration stylesheets so that custom controls resolve their {@code -color-*} lookups.
     */
    public boolean isAtlantaFX() {
        return atlantafxTheme != null;
    }
}
