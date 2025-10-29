package com.wlf.app.main.data;

import lombok.Getter;

@Getter
public enum Type {
    MAP("Map"), // json prop "type" 0
    MOD("Mod"), // json prop "type" 1
    SKIN("Skin"), // json prop "type" 2
    RESOURCE("Resource"), // json prop "type" 3
    DLL("Library files (.dll)"), // json prop "type" 4
    UNDEFINED("undefined"); // json prop "type" 99

    Type(String name) { this.name = name; }
    private final String name;

    public int getFlag() {
        return 1 << this.ordinal();
    }

    public static boolean contains(int mask, Type type) {
        return (mask & type.getFlag()) != 0;
    }

    public static boolean matches(int mask, int otherMask) {
        return (mask & otherMask) == otherMask;
    }

    @Override
    public String toString() {
        return getName();
    }
}
