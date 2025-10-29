package com.wlf.app;

import atlantafx.base.theme.*;
import com.wlf.common.themes.Caspian;
import com.wlf.common.themes.Modena;
import com.wlf.common.themes.ModenaDark;
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
        DRACULA(new Dracula()),
        MODENA(new Modena()),
        MODENA_DARK(new ModenaDark()),
        CASPIAN(new Caspian());

        Theme(atlantafx.base.theme.Theme theme) { this.theme = theme; }
        @Getter
        final atlantafx.base.theme.Theme theme;

        @Override
        public String toString() {
            return this.theme.getName();
        }
    }

    private String name;
    private String file;
    private boolean useCustomDecorations;
    private boolean isActive;
}
