package com.wlf.common.util;

import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeStringConverter extends StringConverter<LocalDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    @Override
    public String toString(LocalDateTime localDateTime) {
        return localDateTime == null ? "" : formatter.format(localDateTime);
    }

    @Override
    public LocalDateTime fromString(String s) {
        return s == null || s.isBlank() ? null : LocalDateTime.parse(s, formatter);
    }
}
