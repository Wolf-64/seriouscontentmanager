package com.wlf.app.main.data;

import java.util.Date;

import javafx.beans.property.*;
import javafx.beans.value.WritableObjectValue;

public class Filter {
    // checkboxes
    private final IntegerProperty gameSelected = new SimpleIntegerProperty();
    private final IntegerProperty typeSelected = new SimpleIntegerProperty();
    private final IntegerProperty modeSelected = new SimpleIntegerProperty();

    private final BooleanProperty installed = new SimpleBooleanProperty();
    private final BooleanProperty completed = new SimpleBooleanProperty();
    private final StringProperty name = new SimpleStringProperty();

    private Date dateAdded;
    private Date dateCompleted;

    public boolean anyGame() {
        return getGameSelected() == Game.ANY.ordinal();
    }

    public boolean anyType() {
        return getTypeSelected() == Type.UNDEFINED.ordinal();
    }

    public boolean anyMode() {
        return getModeSelected() == Mode.ALL.ordinal();
    }

    public boolean isInstalled() {
        return installed.get();
    }

    public BooleanProperty installedProperty() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed.set(installed);
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed.set(completed);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getGameSelected() {
        return gameSelected.get();
    }

    public IntegerProperty gameSelectedProperty() {
        return gameSelected;
    }

    public void setGameSelected(int gameSelected) {
        this.gameSelected.set(gameSelected);
    }

    public int getTypeSelected() {
        return typeSelected.get();
    }

    public IntegerProperty typeSelectedProperty() {
        return typeSelected;
    }

    public void setTypeSelected(int typeSelected) {
        this.typeSelected.set(typeSelected);
    }

    public int getModeSelected() {
        return modeSelected.get();
    }

    public IntegerProperty modeSelectedProperty() {
        return modeSelected;
    }

    public void setModeSelected(int modeSelected) {
        this.modeSelected.set(modeSelected);
    }
}
