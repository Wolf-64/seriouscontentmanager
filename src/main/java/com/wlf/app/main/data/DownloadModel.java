package com.wlf.app.main.data;

import com.wlf.app.main.net.Downloader;
import com.wlf.common.BaseModel;
import javafx.beans.property.*;
import lombok.Getter;
import lombok.Setter;

public class DownloadModel extends BaseModel {
    private final DoubleProperty progress = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty("Downloading...");
    private final StringProperty downloadURL = new SimpleStringProperty();
    private final StringProperty maxMb = new SimpleStringProperty();
    private final StringProperty downloadedMb = new SimpleStringProperty();
    private final StringProperty estimatedTime = new SimpleStringProperty();
    private final StringProperty fileName = new SimpleStringProperty();

    @Getter @Setter
    private Downloader downloader;

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public String getMaxMb() {
        return maxMb.get();
    }

    public StringProperty maxMbProperty() {
        return maxMb;
    }

    public void setMaxMb(String maxMb) {
        this.maxMb.set(maxMb);
    }

    public String getDownloadedMb() {
        return downloadedMb.get();
    }

    public StringProperty downloadedMbProperty() {
        return downloadedMb;
    }

    public void setDownloadedMb(String downloadedMb) {
        this.downloadedMb.set(downloadedMb);
    }

    public String getEstimatedTime() {
        return estimatedTime.get();
    }

    public StringProperty estimatedTimeProperty() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime.set(estimatedTime);
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public String getDownloadURL() {
        return downloadURL.get();
    }

    public StringProperty downloadURLProperty() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL.set(downloadURL);
    }
}
