package com.wlf.common.util;

public class OSUtil {
    public enum OS { UNKNOWN, WIN, LINUX, MAC }

    public static OS getOS() {
        String osNameProp = System.getProperty("os.name").toLowerCase();
        if (osNameProp.contains("win")) {
            return OS.WIN;
        } else if (osNameProp.contains("linux")) {
            return OS.LINUX;
        } else if (osNameProp.contains("darwin") || osNameProp.contains("mac")) {
            return OS.MAC;
        } else {
            return OS.UNKNOWN;
        }
    }
}
