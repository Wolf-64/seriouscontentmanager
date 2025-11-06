package com.wlf.app.main.data;

import javafx.beans.property.*;
import org.mapstruct.Named;

public class PropertyMapper {

    @Named("stringToProperty")
    public StringProperty stringToProperty(String value) {
        return new SimpleStringProperty(value);
    }

    @Named("propertyToString")
    public String propertyToString(StringProperty prop) {
        return prop.get();
    }

    @Named("booleanToProperty")
    public BooleanProperty booleanToProperty(boolean value) {
        return new SimpleBooleanProperty(value);
    }

    @Named("propertyToBoolean")
    public boolean propertyToBoolean(BooleanProperty prop) {
        return prop.get();
    }

    @Named("longToProperty")
    public LongProperty longToProperty(Long value) {
        return new SimpleLongProperty(value != null ? value : 0L);
    }

    @Named("propertyToLong")
    public Long propertyToLong(LongProperty prop) {
        return prop.get();
    }
}
