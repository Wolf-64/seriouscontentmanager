package com.wlf.app.main.data;

import com.wlf.app.main.io.FileHandler;
import com.wlf.app.preferences.Config;
import lombok.Getter;

import java.io.File;
import java.util.Arrays;


@Getter
public enum Game {
    TFE("The First Encounter"), // json prop "game" 0
    TSE("The Second Encounter"), // json prop "game" 1
    ANY("Any"); // json prop "game" 2

    Game(String name) { this.name = name; }
    private final  String name;

    public String getGameFolder() {
        return switch (this) {
            case TFE -> Config.getInstance().getDirectoryTFE();
            case TSE -> Config.getInstance().getDirectoryTSE();
            case ANY -> null;
        };
    }

    public boolean isGamePathValid(String path) {
        if (path != null && new File(path).exists()) {
            if (!this.equals(ANY)) {
                return Arrays.stream(getKnownFiles()).allMatch(File::exists);
            }
        }
        return false;
    }

    public File[] getKnownFiles() {
        return switch (this) {
            case TFE -> new File[]{
                    new File(Config.getInstance().getDirectoryTFE() + File.separator + "Bin" + File.separator + "SeriousSam.exe"),
                    new File(Config.getInstance().getDirectoryTFE() + File.separator + "1_00_ExtraTools.gro"),
                    new File(Config.getInstance().getDirectoryTFE() + File.separator + "1_00_music.gro"),
                    new File(Config.getInstance().getDirectoryTFE() + File.separator + "1_00c_Logo.gro"),
                    new File(Config.getInstance().getDirectoryTFE() + File.separator + "1_00c_scripts.gro"),
                    new File(Config.getInstance().getDirectoryTFE() + File.separator + "1_00c.gro"),
                    new File(Config.getInstance().getDirectoryTFE() + File.separator + "1_04_patch.gro")
            };
            case TSE -> new File[]{
                    new File(Config.getInstance().getDirectoryTSE() + File.separator + "Bin" + File.separator + "SeriousSam.exe"),
                    new File(Config.getInstance().getDirectoryTSE() + File.separator + "1_04_patch.gro"),
                    new File(Config.getInstance().getDirectoryTSE() + File.separator + "1_07_tools.gro"),
                    new File(Config.getInstance().getDirectoryTSE() + File.separator + "SE1_00_Extra.gro"),
                    new File(Config.getInstance().getDirectoryTSE() + File.separator + "SE1_00_ExtraTools.gro"),
                    new File(Config.getInstance().getDirectoryTSE() + File.separator + "SE1_00_Levels.gro"),
                    new File(Config.getInstance().getDirectoryTSE() + File.separator + "SE1_00_Logo.gro"),
                    new File(Config.getInstance().getDirectoryTSE() + File.separator + "SE1_00_Music.gro"),
                    new File(Config.getInstance().getDirectoryTSE() + File.separator + "SE1_00.gro"),
                    new File(Config.getInstance().getDirectoryTSE() + File.separator + "ModEXT.txt")
            };
            case ANY -> null;
        };
    }

    public int getFlag() {
        return 1 << this.ordinal();
    }

    public static boolean contains(int mask, Game game) {
        return (mask & game.getFlag()) != 0;
    }

    public static boolean matches(int mask, int otherMask) {
        return (mask & otherMask) == otherMask;
    }

    @Override
    public String toString() {
        return getName();
    }
}
