package com.wlf.app.preferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wlf.common.BaseModel;
import javafx.beans.property.*;
import lombok.Getter;

/**
 * Global configuration object containing general settings and tool-specific configurations.
 * Is loaded from and stored to JSON for simplicity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagerConfig extends BaseModel {
    private final BooleanProperty useSteamRuntime = new SimpleBooleanProperty(false);
    private final StringProperty directoryTFE = new SimpleStringProperty();
    private final StringProperty directoryTSE = new SimpleStringProperty();
    @Getter
    private boolean[] tableColumnHeaders = new boolean[13];

    @JsonIgnore
    private final BooleanProperty tfeDirectoryValid = new SimpleBooleanProperty();
    @JsonIgnore
    private final BooleanProperty tseDirectoryValid = new SimpleBooleanProperty();

    // --- JavaFX boilerplate that's not covered by Lombok ---

    public String getDirectoryTFE() {
        return directoryTFE.get();
    }

    public void setDirectoryTFE(String directoryTFE) {
        this.directoryTFE.set(directoryTFE);
    }

    public StringProperty directoryTFEProperty() {
        return directoryTFE;
    }
    public String getDirectoryTSE() {
        return directoryTSE.get();
    }

    public void setDirectoryTSE(String directoryTSE) {
        this.directoryTSE.set(directoryTSE);
    }

    public StringProperty directoryTSEProperty() {
        return directoryTSE;
    }

    public boolean isTfeDirectoryValid() {
        return tfeDirectoryValid.get();
    }

    public BooleanProperty tfeDirectoryValidProperty() {
        return tfeDirectoryValid;
    }

    public void setTfeDirectoryValid(boolean tfeDirectoryValid) {
        this.tfeDirectoryValid.set(tfeDirectoryValid);
    }

    public boolean isTseDirectoryValid() {
        return tseDirectoryValid.get();
    }

    public BooleanProperty tseDirectoryValidProperty() {
        return tseDirectoryValid;
    }

    public void setTseDirectoryValid(boolean tseDirectoryValid) {
        this.tseDirectoryValid.set(tseDirectoryValid);
    }

    public boolean isUseSteamRuntime() {
        return useSteamRuntime.get();
    }

    public BooleanProperty useSteamRuntimeProperty() {
        return useSteamRuntime;
    }

    public void setUseSteamRuntime(boolean useSteamRuntime) {
        this.useSteamRuntime.set(useSteamRuntime);
    }
}

