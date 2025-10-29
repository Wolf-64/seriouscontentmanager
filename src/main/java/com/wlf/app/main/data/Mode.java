package com.wlf.app.main.data;

import lombok.Getter;

@Getter
public enum Mode {
    SP("Singleplayer"), // ModeID 0
    COOP("Co-op"), // ModeID 1
    DM("Deathmatch"), // ModeID 2
    UNDEFINED("All modes"), // ModeID 3
    ALL("All modes"); // ModeID 3

    Mode(String name) { this.name = name; }
    private final  String name;

    public int getFlag() {
        return 1 << this.ordinal();
    }

    public static boolean contains(int mask, Mode mode) {
        return (mask & mode.getFlag()) != 0;
    }

    public static boolean matches(int mask, int otherMask) {
        return (mask & otherMask) == otherMask;
    }

    @Override
    public String toString() {
        return getName();
    }
}
