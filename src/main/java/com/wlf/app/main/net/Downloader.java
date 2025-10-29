package com.wlf.app.main.net;

import com.wlf.common.controls.AccentedProgressBar;
import com.wlf.app.main.data.ContentModel;
import lombok.Setter;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Downloader extends Thread implements Runnable {
    public static final Logger log = Logger.getLogger(Downloader.class.getSimpleName());

    private ContentModel currentDownloadingContentModel;
    private String url;
    private String filename;
    private String destinationFolder;

    private long lastUpdateTime;
    @Setter
    private long updateIntervalMillis;

    private boolean stop;
    @Setter
    private volatile boolean pause;

    @Setter
    private BiConsumer<String, ContentModel> onCompleted;
    @Setter
    private Consumer<ContentModel> onStart;
    @Setter
    private UpdateCallback onProgress;
    @Setter
    private Consumer<Exception> onError;

    public void download(String modName, String downloadDirectory) {
        URI downloadURI = null;
        // fetch metadata and request download
        com.fasterxml.jackson.databind.JsonNode modInfoJson;
        try (Requester requester = new Requester()) {
            modInfoJson = requester.requestModInfo(modName);

            currentDownloadingContentModel = new ContentModel().fromJSON(modInfoJson);
            downloadURI = requester.requestDownloadURI(currentDownloadingContentModel.getRepoId());

            if (downloadURI != null) {
                Requester.FileInfo fileInfo = requester.requestFileInfo(downloadURI);
                int totalFileSize = fileInfo.getTotalFileSize();
                filename = /*downloadDirectory + "/" + */fileInfo.getFileName();
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
        url = downloadURI.toString();

        super.start();
    }

    public void run(){
        try {
            if (onStart != null) {
                onStart.accept(currentDownloadingContentModel);
            }

            URL url = URI.create(this.url).toURL();
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();

            long total = urlConnection.getContentLengthLong();
            int count;

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(destinationFolder + File.separator + filename);

            byte[] data = new byte[4096];
            long current = 0;

            while ((count = input.read(data)) != -1) {
                if (stop) {
                    break;
                }

                current += count;
                output.write(data, 0, count);
                if (System.currentTimeMillis() - lastUpdateTime > updateIntervalMillis) {
                    lastUpdateTime = System.currentTimeMillis();
                    if (onProgress != null) {
                        onProgress.accept(total, current, ((double) current / total));
                    }
                }

                while (pause) {
                    Thread.onSpinWait();
                }
            }

            output.flush();

            output.close();
            input.close();

            if (onCompleted != null && !stop) {
                if (onProgress != null) {
                    onProgress.accept(1, 1, 1.0);
                }
                onCompleted.accept(destinationFolder + File.separator + filename, currentDownloadingContentModel);
            }

            if (stop) {
                if (onProgress != null) {
                    onProgress.accept(total, current, AccentedProgressBar.WARNING_PROGRESS);
                }
                if (onCompleted != null) {
                    onCompleted.accept(null, null);
                }
                Files.delete(Path.of(destinationFolder + File.separator + filename));
            }
        } catch (Exception e) {
            if (onError != null) {
                onError.accept(e);
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
