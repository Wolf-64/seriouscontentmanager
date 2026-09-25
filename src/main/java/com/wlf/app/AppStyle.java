package com.wlf.app;

import com.dlsc.atlantafx.themes.*;
import com.wlf.common.themes.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AppStyle {
    public enum Theme {
        // AtlantaFX built-ins
        PRIMER(fx(new atlantafx.base.theme.PrimerLight()), fx(new atlantafx.base.theme.PrimerDark())),
        NORD(fx(new atlantafx.base.theme.NordLight()), fx(new atlantafx.base.theme.NordDark())),
        CUPERTINO(fx(new atlantafx.base.theme.CupertinoLight()), fx(new atlantafx.base.theme.CupertinoDark())),
        DRACULA(null, fx(new atlantafx.base.theme.Dracula())),
        // DLSC themes
        SPRING(fx(new SpringLight()), fx(new SpringDark())),
        SUMMER(fx(new SummerLight()), fx(new SummerDark())),
        FALL(fx(new FallLight()), fx(new FallDark())),
        WINTER(fx(new WinterLight()), fx(new WinterDark())),
        BLUE(fx(new BlueLight()), fx(new BlueDark())),
        NAVY(fx(new NavyLight()), fx(new NavyDark())),
        ARMY(fx(new ArmyLight()), fx(new ArmyDark())),
        AUTUMN(null, fx(new Autumn())),
        BLACKY(null, fx(new Blacky())),
        BROWNY(null, fx(new Browny())),
        NEWS(null, fx(new News())),
        YACHT(fx(new Yacht()), null),
        GITHUB(fx(new GithubLightDefault()), fx(new GithubSoftDark())),
        GITHUB_COLORBLIND(fx(new GithubLightColorblind()), fx(new GithubDarkColorblind())),
        GITHUB_TRITANOPIA(fx(new GithubLightTritanopia()), fx(new GithubDarkTritanopia())),
        // FX base themes
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

        private static BaseTheme fx(atlantafx.base.theme.Theme delegate) {
            return new DelegateTheme(delegate);
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
