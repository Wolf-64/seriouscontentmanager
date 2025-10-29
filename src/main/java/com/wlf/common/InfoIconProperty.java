package com.wlf.common;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.kordamp.ikonli.Ikon;

public class InfoIconProperty extends SimpleObjectProperty<InfoIconProperty> {
    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>();
    private final StringProperty info = new SimpleStringProperty();

    private InfoIconProperty() {}

    public InfoIconProperty(Ikon defaultIcon) {
        this(defaultIcon, null);
    }

    public InfoIconProperty(Ikon defaultIcon, String info) {
        this.icon.set(defaultIcon);
        this.info.set(info);
    }

    @Override
    protected void fireValueChangedEvent() {
        super.fireValueChangedEvent();
    }

    @Override
    public InfoIconProperty getValue() {
        return get();
    }
    @Override
    public InfoIconProperty get() {
        return this;
    }

    public Ikon getIcon() {
        return icon.get();
    }

    public ObjectProperty<Ikon> iconProperty() {
        return icon;
    }

    public void setIcon(Ikon icon) {
        this.icon.set(icon);
        fireValueChangedEvent();
    }

    public String getInfo() {
        return info.get();
    }

    public StringProperty infoProperty() {
        return info;
    }

    public void setInfo(String info) {
        this.info.set(info);
        fireValueChangedEvent();
    }
}
