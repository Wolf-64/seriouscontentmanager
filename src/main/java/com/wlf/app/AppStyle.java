package com.wlf.app;

import com.wlf.common.themes.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AppStyle {
    public enum Theme {
        PRIMER(new PrimerLight(), new PrimerDark()),
        NORD(new NordLight(), new NordDark()),
        CUPERTINO(new CupertinoLight(), new CupertinoDark()),
        DRACULA(new Dracula(), new Dracula()),
        MODENA(new Modena(), new ModenaDark()),
        CASPIAN(new Caspian(), new Caspian());

        @Getter
        final BaseTheme lightTheme;
        @Getter
        final BaseTheme darkTheme;

        Theme(BaseTheme lightTheme,  BaseTheme darkTheme) {
            this.lightTheme = lightTheme;
            this.darkTheme = darkTheme;
        }

        @Override
        public String toString() {
            if (lightTheme != null) {
                return lightTheme.getName().replace("Light", "");
            } else if (darkTheme != null) {
                return darkTheme.getName().replace("Dark", "");
            }
            return "undefined";
        }
    }

    private String name;
    private String file;
    private boolean useCustomDecorations;
    private boolean isActive;

    public AppStyle() {}

    public AppStyle(String name, String file, boolean useCustomDecorations, boolean isActive) {
        this.name = name;
        this.file = file;
        this.useCustomDecorations = useCustomDecorations;
        this.isActive = isActive;
    }

    public AppStyle(String name, String file, boolean useCustomDecorations) {
        this(name, file, useCustomDecorations, false);
    }

    @Override
    public String toString() {
        return name + (isActive ? " (current)" : "");
    }
}
