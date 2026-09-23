package com.wlf.app.preferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wlf.app.AppStyle;
import com.wlf.common.BaseModel;
import javafx.beans.property.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Global configuration object containing general settings and tool-specific configurations.
 * Is loaded from and stored to JSON for simplicity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config extends BaseModel {
    private final ObjectProperty<Integer> maxDownloads = new SimpleObjectProperty<>(2);
    private final BooleanProperty autoClearFinishedDownloads = new SimpleBooleanProperty(false);
    private final BooleanProperty useSteamRuntime = new SimpleBooleanProperty(false);
    private final StringProperty directoryDownloads = new SimpleStringProperty();
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
    public String getDirectoryDownloads() {
        return directoryDownloads.get();
    }

    public void setDirectoryDownloads(String directoryTFE) {
        this.directoryDownloads.set(directoryTFE);
    }

    public StringProperty directoryDownloadsProperty() {
        return directoryDownloads;
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

    public Integer getMaxDownloads() {
        return maxDownloads.get();
    }

    public ObjectProperty<Integer> maxDownloadsProperty() {
        return maxDownloads;
    }

    public void setMaxDownloads(Integer maxDownloads) {
        this.maxDownloads.set(maxDownloads);
    }

    public boolean isAutoClearFinishedDownloads() {
        return autoClearFinishedDownloads.get();
    }

    public BooleanProperty autoClearFinishedDownloadsProperty() {
        return autoClearFinishedDownloads;
    }

    public void setAutoClearFinishedDownloads(boolean autoClearFinishedDownloads) {
        this.autoClearFinishedDownloads.set(autoClearFinishedDownloads);
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

