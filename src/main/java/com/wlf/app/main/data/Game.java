package com.wlf.app.main.data;

import com.wlf.app.preferences.Config;
import lombok.Getter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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
        if (path != null && Files.exists(Path.of(path))) {
            if (!this.equals(ANY)) {
                return Arrays.stream(getKnownFiles(path)).allMatch(File::exists);
            }
        }
        return false;
    }

    public File[] getKnownFiles(String basePath) {
        return switch (this) {
            case TFE -> new File[]{
                    Path.of(basePath, "Bin", "SeriousSam.exe").toFile(),
                    Path.of(basePath, "1_00_ExtraTools.gro").toFile(),
                    Path.of(basePath, "1_00_music.gro").toFile(),
                    Path.of(basePath, "1_00c_Logo.gro").toFile(),
                    Path.of(basePath, "1_00c_scripts.gro").toFile(),
                    Path.of(basePath, "1_00c.gro").toFile(),
                    Path.of(basePath, "1_04_patch.gro").toFile()
            };
            case TSE -> new File[]{
                    Path.of(basePath, "Bin", "SeriousSam.exe").toFile(),
                    Path.of(basePath, "1_04_patch.gro").toFile(),
                    Path.of(basePath, "1_07_tools.gro").toFile(),
                    Path.of(basePath, "SE1_00_Extra.gro").toFile(),
                    Path.of(basePath, "SE1_00_ExtraTools.gro").toFile(),
                    Path.of(basePath, "SE1_00_Levels.gro").toFile(),
                    Path.of(basePath, "SE1_00_Logo.gro").toFile(),
                    Path.of(basePath, "SE1_00_Music.gro").toFile(),
                    Path.of(basePath, "SE1_00.gro").toFile(),
                    Path.of(basePath, "ModEXT.txt").toFile()
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
