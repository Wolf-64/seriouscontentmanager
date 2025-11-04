package com.wlf.app.main.net;

import com.wlf.common.controls.AccentedProgressBar;
import com.wlf.app.main.data.ContentModel;
import lombok.Setter;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Downloader extends Thread implements Runnable {
    public static final Logger log = Logger.getLogger(Downloader.class.getSimpleName());

    private ContentModel currentDownloadingContentModel;
    private URI downloadURI;
    private String filename;
    private String destinationFolder;

    private long lastUpdateTime;
    @Setter
    private long updateIntervalMillis;

    private boolean stop;

    private final Object pauseLock = new Object();
    private volatile boolean pause = false;

    @Setter
    private BiConsumer<String, ContentModel> onCompleted;
    @Setter
    private Consumer<ContentModel> onStart;
    @Setter
    private UpdateCallback onProgress;
    @Setter
    private Consumer<Exception> onError;

    public Downloader(URI fileUri) {
        this.downloadURI = fileUri;
    }

    public void download(ModInfo modInfo, int linkIndex, String downloadDirectory) {
        // fetch metadata and request download
        try (Requester requester = new Requester()) {
            currentDownloadingContentModel = new ContentModel().fromModInfo(modInfo);
            downloadURI = requester.requestDownloadURI(modInfo.getId(), modInfo.getLinks().get(linkIndex).getType());

            if (downloadURI != null) {
                Requester.FileInfo fileInfo = requester.requestFileInfo(downloadURI);
                filename = fileInfo.getFileName();
                currentDownloadingContentModel.setDownloadedFileName(filename);
            } else {
                return;
            }
        } catch (IOException | InterruptedException e) {
            if (onError != null) {
                onError.accept(e);
            }
            return;
        }

        destinationFolder = downloadDirectory;

        super.start();
    }

    public void run() {
        try {
            if (onStart != null) {
                onStart.accept(currentDownloadingContentModel);
            }

            URL url = downloadURI.toURL();

            long totalBytes = url.openConnection().getContentLengthLong();
            if (totalBytes <= 0) totalBytes = -1; // sometimes we just don't know :(

            try (ReadableByteChannel src = Channels.newChannel(url.openStream());
                 FileOutputStream fos = new FileOutputStream(Path.of(destinationFolder, filename).toFile());
                 WritableByteChannel dest = fos.getChannel()) {

                ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
                long bytesReadTotal = 0;

                while (src.read(buffer) != -1) {
                    buffer.flip();
                    bytesReadTotal += dest.write(buffer);
                    buffer.clear();

                    if (System.currentTimeMillis() - lastUpdateTime > updateIntervalMillis) {
                        lastUpdateTime = System.currentTimeMillis();
                        if (onProgress != null) {
                            onProgress.accept(totalBytes, bytesReadTotal, ((double) bytesReadTotal / totalBytes));
                        }
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

                if (onCompleted != null && !stop) {
                    if (onProgress != null) {
                        onProgress.accept(1, 1, 1.0);
                    }
                    onCompleted.accept(destinationFolder + File.separator + filename, currentDownloadingContentModel);
                }
            }
        } catch (Exception e) {
            if (onError != null) {
                onError.accept(e);
            }
        } finally {
            if (stop) {
                if (onProgress != null) {
                    onProgress.accept(1, 1, AccentedProgressBar.WARNING_PROGRESS);
                }
                if (onCompleted != null) {
                    onCompleted.accept(null, null);
                }

                try {
                    Files.deleteIfExists(Path.of(destinationFolder + File.separator + filename));
                } catch (IOException ignored) { }
            }
        }
    }

    public void setPause(boolean value) {
        pause = value;
        if (!value) {
            synchronized (pauseLock) {
                pauseLock.notifyAll();
            }
        }
    }

    public void stopDownload() {
        stop = true;
    }

    private void onDownloadStopped() {

    }

    @FunctionalInterface
    public interface UpdateCallback {
        void accept(long fileSize, long bytesReceived, double progressPercent);
    }
}
