package com.wlf.common.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class LocalDateConverter extends StringConverter<LocalDate> implements AttributeConverter<LocalDate, String> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");

    @Override
    public String convertToDatabaseColumn(LocalDate value) {
        return value == null ? null : value.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    @Override
    public String toString(LocalDate localDateTime) {
        return localDateTime == null ? "" : formatter.format(localDateTime);
    }

    @Override
    public LocalDate fromString(String s) {
        return s == null || s.isBlank() ? null : LocalDate.parse(s, formatter);
    }
}