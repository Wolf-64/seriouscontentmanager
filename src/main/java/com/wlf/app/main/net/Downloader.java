package com.wlf.app.main.net;

import com.wlf.app.main.data.ContentFile;
import com.wlf.app.main.data.ContentEntity;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.logging.Logger;

public class Downloader extends Task<ContentEntity> {
    public static final Logger log = Logger.getLogger(Downloader.class.getSimpleName());

    private URI downloadURI;

    private long lastUpdateTime;
    @Setter
    private long updateIntervalMillis;

    private boolean stop;

    private final Object pauseLock = new Object();
    private volatile boolean pause = false;

    @Getter
    private final ModInfo modInfo;
    private final String downloadDirectory;

    private String baseMessage;

    public Downloader(ModInfo modInfo, URI fileUri, String downloadDirectory) {
        this.modInfo = modInfo;
        this.downloadURI = fileUri;
        this.downloadDirectory = downloadDirectory;
        baseMessage = "Waiting to download \"" + modInfo.getTitle() + "\" ...";
        updateMessage(baseMessage);
    }

    @Override
    protected ContentEntity call() throws Exception {
        // fetch metadata and request download
        String filename;
        ContentEntity currentDownloadingContentEntity;
        try (Requester requester = new Requester()) {
            currentDownloadingContentEntity = new ContentEntity().fromModInfo(modInfo);
            if (downloadURI != null) {
                Requester.FileInfo fileInfo = requester.requestFileInfo(downloadURI);
                filename = fileInfo.getFileName();
                currentDownloadingContentEntity.setDownloadedFileName(filename);
                currentDownloadingContentEntity.setDownloadedFile(new ContentFile(Path.of(downloadDirectory, filename).toString()));
            } else {
                throw new Exception("Could not get file info!");
            }

        }

        baseMessage = "Downloading \"" + filename + "\"";
        updateMessage(baseMessage);

        URL url = downloadURI.toURL();

        long totalBytes = url.openConnection().getContentLengthLong();
        if (totalBytes <= 0) totalBytes = -1; // sometimes we just don't know :(

        try (ReadableByteChannel src = Channels.newChannel(url.openStream());
             FileOutputStream fos = new FileOutputStream(Path.of(downloadDirectory, filename).toFile());
             WritableByteChannel dest = fos.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
            long bytesReadTotal = 0;

            long bytesBetweenUpdate = 0;
            while (src.read(buffer) != -1) {
                buffer.flip();
                int bytesRead = dest.write(buffer);
                bytesReadTotal += bytesRead;
                bytesBetweenUpdate += bytesRead;
                buffer.clear();

                if (System.currentTimeMillis() - lastUpdateTime > updateIntervalMillis) {
                    lastUpdateTime = System.currentTimeMillis();
                    updateProgress(bytesReadTotal, totalBytes);
                    updateMessage(baseMessage
                            + " ("
                            + FileUtils.byteCountToDisplaySize(bytesReadTotal)
                            + "/"
                            + FileUtils.byteCountToDisplaySize(totalBytes)
                            +" @"
                            + FileUtils.byteCountToDisplaySize(bytesBetweenUpdate)
                            + "/s)");
                    bytesBetweenUpdate = 0;
                }

                while (pause) {
                    synchronized (pauseLock) {
                        pauseLock.wait();
                    }
                }

                if (stop) {
                    break;
                }
            }
        }
        updateProgress(totalBytes, totalBytes);
        updateMessage(baseMessage + " ... done!");
        done();
        return currentDownloadingContentEntity;
    }

    public void setPause(boolean value) {
        pause = value;
        updateMessage(baseMessage + " ... paused");
        if (!value) {
            synchronized (pauseLock) {
                pauseLock.notifyAll();
            }
        }
    }

    @Override
    protected void setException(Throwable t) {
        super.setException(t);
        if (getState() != Worker.State.CANCELLED) {
            updateMessage(baseMessage + " ... failed: " + t.getLocalizedMessage());
            done();
        }
    }

    public void stopDownload() {
        stop = true;
    }

    @Override
    public boolean cancel(boolean cancel) {
        if (cancel) {
            updateMessage(baseMessage + " ... download cancelled.");
            updateProgress(0, 1);
        }
        return super.cancel(cancel);
    }
}
