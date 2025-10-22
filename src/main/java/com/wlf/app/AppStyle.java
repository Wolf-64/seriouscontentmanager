package com.wlf.app;

import atlantafx.base.theme.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppStyle {
    public enum Theme {
        PRIMER_LIGHT(new PrimerLight()),
        PRIMER_DARK(new PrimerDark()),
        NORD_LIGHT(new NordLight()),
        NORD_DARK(new NordDark()),
        CUPERTINO_LIGHT(new CupertinoLight()),
        CUPERTINO_DARK(new CupertinoDark()),
        DRACULA(new Dracula());

        Theme(atlantafx.base.theme.Theme theme) { this.theme = theme; }
        final atlantafx.base.theme.Theme theme;
    }

    private String name;
    private String file;
    private boolean useCustomDecorations;
    private boolean isActive;
}
