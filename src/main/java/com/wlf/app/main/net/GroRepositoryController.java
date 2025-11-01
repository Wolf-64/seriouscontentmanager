package com.wlf.app.main.net;

import com.wlf.app.App;
import com.wlf.app.main.data.*;
import com.wlf.app.main.io.FileHandler;
import com.wlf.common.BaseController;
import com.wlf.common.controls.AccentedProgressBar;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Logger;

public class GroRepositoryController extends BaseController<DataModel> {
    static Logger log = Logger.getLogger(GroRepositoryController.class.getSimpleName());

    @FXML
    @Getter
    @Setter
    private WebView webView;

    @FXML
    private BorderPane webViewContainer;

    private final ObjectProperty<WebEngine> browser = new SimpleObjectProperty<>();

    @FXML
    ProgressBar downloadProgress;

    @Getter
    @Setter
    private ObservableList<DownloadModel> activeDonwloads = FXCollections.observableArrayList();

    private final BooleanProperty stopDownloadButtonDisabled = new SimpleBooleanProperty(true);

    private boolean downloadInProgress;

    private String lastLocation;

    private final String GRO_REPOSITORY_URL = "https://grorepository.ru/mods";
    private final String GRO_MODPAGE_URL = "https://grorepository.ru/mod/";
    private final String ABOUT_BLANK = "about:blank";
    private final String TMP_DONWLOADS = "tmpDownload";

    @Override
    protected void initialize() {

    }

    // need to defer this to runtime as the WebView must be instantiated on the FX application thread
    public void initWebView() {
        webView = new WebView();
        webViewContainer.setCenter(webView);
        browser.set(webView.getEngine());
        browser.get().locationProperty().addListener((observable -> {
            //check for url here?
            try {
                lastLocation = browser.get().getHistory().getEntries().getLast().getUrl();
                // after clicking on download we'll just get a white screen
                if (ABOUT_BLANK.equals(browser.get().getLocation())
                        && lastLocation.startsWith(GRO_MODPAGE_URL)) {
                    onDownloadRequestReceived();
                }
            } catch (NoSuchElementException ignored) {
            }
        }));

        App.FRAME_CONTROLLER.setLoading(false);
    }

    public void loadGroRepo() {
        browser.get().load(GRO_REPOSITORY_URL);
    }

    private void onDownloadRequestReceived() {
        // Check for temp download dir
        Path path = Path.of(TMP_DONWLOADS);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                App.showError(e);
                return;
            }
        }

        String modName = lastLocation.substring(lastLocation.lastIndexOf('/') + 1);

        ModInfo modInfo = null;
        ContentLanguage language = ContentLanguage.DEFAULT_OR_RU;
        int linkIndex = 0;
        URI downloadURI = null;
        try (Requester requester = new Requester()) {
            modInfo = requester.requestModInfo(modName);
            if (modInfo.getLinks().size() > 1) {
                List<ContentLanguage> availableLanguages = modInfo.getLinks().stream()
                        .map(ModInfo.Link::getType)
                        .toList();
                ChoiceDialog<ContentLanguage> dlg = new ChoiceDialog<>();
                dlg.getItems().setAll(availableLanguages);
                dlg.initOwner(App.MAINSTAGE.getOwner());
                dlg.setTitle("Language Select");
                dlg.getDialogPane().setContentText("Select language:");
                Optional<ContentLanguage> result = dlg.showAndWait();
                if (result.isPresent()) {
                    language = result.get();
                    ContentLanguage finalLanguage = language;
                    Optional<ModInfo.Link> link = modInfo.getLinks().stream()
                            .filter(lnk -> lnk.getType() == finalLanguage)
                            .findFirst();
                    if (link.isPresent()) {
                        linkIndex = modInfo.getLinks().indexOf(link.get());
                    }
                }
            }
            downloadURI = requester.requestDownloadURI(modInfo.getId(), language);
        } catch (IOException | InterruptedException e) {
            App.showError(e);
            return;
        }

        // check for already active download of the same file
        if (activeDonwloads.stream().anyMatch(download -> download.getDownloadURL().equals(lastLocation))) {
            log.warning("Content '" + modName + "' already in download list.");
            new Alert(Alert.AlertType.WARNING, "File already downloading!").show();
            browser.get().load(lastLocation);
            //onBrowserBack(null);
            return;
        }

        // check existence in model
        ModInfo finalModInfo = modInfo;
        if (getModel().getContent().stream().anyMatch(content ->
                content.getName().equals(finalModInfo.getTitle()))) {
            log.warning("Content '" + modName + "' already in download list.");
            new Alert(Alert.AlertType.INFORMATION, "File already in library!").show();
            return;
        }

        // we don't support skins or resources for simplicity
        if (modInfo.getType().ordinal() >= Type.SKIN.ordinal()) {
            new Alert(Alert.AlertType.WARNING, "Content type not supported: " + modInfo.getType().getName()).show();
            browser.get().load(lastLocation);
            return;
        }

        DownloadModel downloadModel = new DownloadModel();
        downloadModel.setFileName(modName);
        downloadModel.setDownloadURL(lastLocation);

        activeDonwloads.add(downloadModel);
        downloadProgress.setProgress(0.0);

        // fire request and download file
        Downloader downloader = new Downloader(downloadURI);
        downloader.setUpdateIntervalMillis(1000L);
        downloader.setOnStart((model) -> {
            downloadModel.setFileName(String.format("%s (%S)", model.getName(), model.getDownloadedFileName()));
        });
        downloader.setOnProgress((totalBytes, bytesReceived, progress) -> {
            onDownloadProgress(downloadModel, totalBytes, bytesReceived, progress);
        });

        //reset progress bar on finish
        downloader.setOnCompleted((filePath, downloadedContentModel) -> {
            onDownloadFinished(downloadModel, filePath, downloadedContentModel);
        });
        downloader.setOnError((e) -> {
            log.severe(e.toString());
            downloadModel.setStatus(e.getMessage());
            downloadModel.setProgress(AccentedProgressBar.FAILED_PROGRESS);
        });
        downloadModel.setDownloader(downloader);
        downloader.download(modInfo, linkIndex, TMP_DONWLOADS);

        downloadInProgress = true;
        setStopDownloadButtonDisabled(false);

        browser.get().load(lastLocation);
    }

    private void onDownloadProgress(DownloadModel downloadModel, long fileSize, long bytesReceived, double progressPercent) {
        Platform.runLater(() -> {
            double totalMb = (double) bytesReceived / (1024 * 1024);
            downloadModel.setDownloadedMb(BigDecimal.valueOf(totalMb).setScale(2, RoundingMode.HALF_UP) + "mb");
            downloadModel.setProgress(progressPercent);
            if (downloadModel.getMaxMb() == null) {
                downloadModel.setMaxMb(BigDecimal.valueOf(fileSize / (1024 * 1024)).setScale(2, RoundingMode.HALF_UP) + "mb");
            }
            downloadProgress.setProgress(progressPercent);
        });
    }

    private void onDownloadFinished(DownloadModel downloadModel, String filePath, ContentModel downloadedContentModel) {
        Platform.runLater(() -> downloadProgress.setProgress(0));
        downloadInProgress = false;
        if (filePath != null) {
            downloadModel.setStatus("Finished!");
            downloadModel.setProgress(AccentedProgressBar.SUCCESSFUL_PROGRESS);
        }
        setStopDownloadButtonDisabled(true);
        // is null when download has been cancelled
        if (filePath != null) {
            FileHandler.registerNewFile(downloadedContentModel, filePath);
            // downloadedContentModel.completedProperty().addListener(getListItemListener(downloadedContentModel));
            getModel().getContent().add(downloadedContentModel);
        }
    }


    @FXML
    public void onBrowserBack(ActionEvent event) {
        try {
            browser.get().getHistory().go(-1);
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @FXML
    public void onBrowserForward(ActionEvent event) {
        try {
            browser.get().getHistory().go(1);
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @FXML
    public void onBrowserRefresh(ActionEvent event) {
        browser.get().reload();
    }

    @FXML
    public void onStopDownloads(ActionEvent event) {
        for (var download : activeDonwloads) {
            if (download.getDownloader() != null) {
                download.getDownloader().stopDownload();
                download.setStatus("Stopped.");
            }
        }
    }

    boolean pauseToggle = false;

    @FXML
    public void onPauseDownloads(ActionEvent event) {
        pauseToggle = !pauseToggle;
        for (var download : activeDonwloads) {
            if (download.getDownloader() != null) {
                download.getDownloader().setPause(pauseToggle);
                download.setStatus(pauseToggle ? "Paused." : "Downloading...");
            }
        }
    }

    // -------------------------------- FX Boilerplate ----------------------------------
    public WebEngine getBrowser() {
        return browser.get();
    }

    public ObjectProperty<WebEngine> browserProperty() {
        return browser;
    }

    public void setBrowser(WebEngine browser) {
        this.browser.set(browser);
    }

    public boolean isStopDownloadButtonDisabled() {
        return stopDownloadButtonDisabled.get();
    }

    public BooleanProperty stopDownloadButtonDisabledProperty() {
        return stopDownloadButtonDisabled;
    }

    public void setStopDownloadButtonDisabled(boolean stopDownloadButtonDisabled) {
        this.stopDownloadButtonDisabled.set(stopDownloadButtonDisabled);
    }
}
