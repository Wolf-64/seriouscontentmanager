package com.wlf.common.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter(autoApply = true)
public class FileListConverter implements AttributeConverter<List<File>, String> {

    @Override
    public String convertToDatabaseColumn(List<File> attribute) {
        if (attribute != null && !attribute.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            attribute.forEach(file -> stringBuilder.append(file.getAbsolutePath()).append(","));
            return stringBuilder.toString();
        } else {
            return "";
        }
    }

    @Override
    public List<File> convertToEntityAttribute(String dbData) {
        if (!dbData.isEmpty()) {
            return Arrays.stream(dbData.split(",")).map(File::new).toList();
        } else {
            return new ArrayList<>();
        }
    }
}
