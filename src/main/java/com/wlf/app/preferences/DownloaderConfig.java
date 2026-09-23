package com.wlf.app.preferences;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wlf.common.BaseModel;
import javafx.beans.property.*;

/**
 * Global configuration object containing general settings and tool-specific configurations.
 * Is loaded from and stored to JSON for simplicity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DownloaderConfig extends BaseModel {
    private final ObjectProperty<Integer> maxDownloads = new SimpleObjectProperty<>(2);
    private final BooleanProperty autoClearFinishedDownloads = new SimpleBooleanProperty(false);
    private final StringProperty directoryDownloads = new SimpleStringProperty();

    // --- JavaFX boilerplate that's not covered by Lombok ---

    public String getDirectoryDownloads() {
        return directoryDownloads.get();
    }

    public void setDirectoryDownloads(String directoryTFE) {
        this.directoryDownloads.set(directoryTFE);
    }

    public StringProperty directoryDownloadsProperty() {
        return directoryDownloads;
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
}

