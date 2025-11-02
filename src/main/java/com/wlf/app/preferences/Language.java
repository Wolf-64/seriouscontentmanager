package com.wlf.app.preferences;

import lombok.Getter;

import java.util.Locale;

public enum Language {
    ENGLISH(Locale.ENGLISH),
    GERMAN(Locale.GERMAN),
    FRENCH(Locale.FRENCH),
    ITALIAN(Locale.ITALIAN),
    SPANISH(Locale.of("es")),
    RUSSIAN(Locale.of("ru"));

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
