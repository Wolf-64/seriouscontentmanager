package com.wlf.common.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.File;
import java.nio.file.Path;

@Converter(autoApply = true)
public class FileConverter implements AttributeConverter<File, String> {

    @Override
    public String convertToDatabaseColumn(File attribute) {
        if (attribute != null) {
            return attribute.getAbsolutePath();
        } else {
            return "";
        }
    }

    @Override
    public File convertToEntityAttribute(String dbData) {
        if (!dbData.isEmpty()) {
            return Path.of(dbData).toFile();
        } else {
            return null;
        }
    }
}
