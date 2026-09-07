package com.example.hr.jpa.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class YnConverter implements AttributeConverter<Boolean, String> {
    @Override
    public String convertToDatabaseColumn(Boolean attr) {
        return (attr != null && attr) ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String db) {
        return "Y".equals(db);
    }
}
