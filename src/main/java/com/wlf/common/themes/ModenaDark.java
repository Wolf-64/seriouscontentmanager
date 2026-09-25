package com.wlf.common.themes;

import com.wlf.common.util.Utils;
import javafx.application.Application;

public class ModenaDark extends BaseTheme {
    @Override
    public String getName() {
        return "Modena Dark";
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
        return true;
    }

    @Override
    public String getSceneStyleSheet() {
        return Utils.getCss("themes/modenaDark.css");
    }
}
