package com.wlf.app.preferences;

import lombok.Getter;

import java.util.Locale;

public enum Language {
    ENGLISH(Locale.ENGLISH),
    GERMAN(Locale.GERMAN);

    @Getter
    final Locale locale;

    Language(Locale locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        return locale.getDisplayLanguage();
    }
}
