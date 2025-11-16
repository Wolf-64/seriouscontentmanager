package com.wlf.common.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class LocalDateTimeConverter extends StringConverter<LocalDateTime> implements AttributeConverter<LocalDateTime, String> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    @Override
    public String convertToDatabaseColumn(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }

    @Override
    public String toString(LocalDateTime localDateTime) {
        return localDateTime == null ? "" : formatter.format(localDateTime);
    }

    @Override
    public LocalDateTime fromString(String s) {
        return s == null || s.isBlank() ? null : LocalDateTime.parse(s, formatter);
    }
}