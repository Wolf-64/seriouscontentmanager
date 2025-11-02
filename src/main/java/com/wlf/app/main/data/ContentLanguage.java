package com.wlf.app.main.data;

import lombok.Getter;

public enum ContentLanguage {
    DEFAULT_OR_RU("русский"),
    UNDEFINED1(""),
    UNDEFINED2(""),
    UNDEFINED3(""),
    EN("English"),
    UNDEFINED5(""),
    UNDEFINED6(""),
    UNDEFINED7(""),
    UNDEFINED8(""),
    UNDEFINED9(""),
    UNDEFINED10("");

    @Getter
    String name;

    ContentLanguage(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return getName();
    }
}
