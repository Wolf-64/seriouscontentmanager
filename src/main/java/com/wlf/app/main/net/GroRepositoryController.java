package com.wlf.app.main.net;

import com.wlf.app.App;
import com.wlf.app.main.data.*;
import com.wlf.app.main.io.FileHandler;
import com.wlf.common.BaseController;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.TaskProgressView;
import org.controlsfx.glyphfont.FontAwesome;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
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
    private TaskProgressView<Task<ContentModel>> downloadTaskView;
    private final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(getConfiguration().getMaxDownloads());
    private final Map<String, Downloader> activeDownloads = new HashMap<>();

    private final BooleanProperty stopDownloadButtonDisabled = new SimpleBooleanProperty(true);
    private final BooleanProperty loadingIndicatorVisible = new SimpleBooleanProperty(false);

    private String lastLocation;

    private final String GRO_REPOSITORY_URL = "https://grorepository.ru/mods";
    private final String GRO_MODPAGE_URL = "https://grorepository.ru/mod/";
    private final String ABOUT_BLANK = "about:blank";
    private final String TMP_DONWLOADS = "tmpDownload";

    @Override
    protected void initialize() {
        FontAwesome fontAwesome = new FontAwesome();
        downloadTaskView.setGraphicFactory(task -> {
            org.controlsfx.glyphfont.Glyph result = fontAwesome.create(FontAwesome.Glyph.DOWNLOAD)
                    .size(24)
                    .color(Color.BLUE);

            result.setEffect(new DropShadow(8, Color.GRAY));
            result.setAlignment(Pos.CENTER);

            /*
             * We have to make sure all glyps have the same size. Otherwise
             * the progress cells will not be aligned properly.
             */
            result.setPrefSize(24, 24);

            return result;
        });
        downloadTaskView.getStylesheets().clear();

        getConfiguration().maxDownloadsProperty().addListener(((observableValue, oldValue, newValue) -> {
            // thread pool value constraints don't allow setting core size higher than max and vice versa, so order matters
            if (oldValue > newValue) {
                executorService.setCorePoolSize(newValue);
                executorService.setMaximumPoolSize(newValue);
            } else {
                executorService.setMaximumPoolSize(newValue);
                executorService.setCorePoolSize(newValue);
            }
        }));

        getOnCloseRequestCallbacks().add(windowEvent -> executorService.shutdown());

        // relevant on reloads - do we have active downloads? need to re-add them to the view
        if (!activeDownloads.isEmpty()) {
            Platform.runLater(() -> downloadTaskView.getTasks().addAll(activeDownloads.values()));
        }
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
    }

    public void loadGroRepo() {
        browser.get().load(GRO_REPOSITORY_URL);
    }

    record DownloadInfo(ModInfo modInfo, URI fileUri, String fileName) {
    }

    private void onDownloadRequestReceived() {
        if (!checkPrerequisitesForDownload()) {
            browser.get().load(lastLocation);
            return;
        }

        // get transliterated mod name to fetch metadata with API
        String modName = lastLocation.substring(lastLocation.lastIndexOf('/') + 1);

        CompletableFuture.supplyAsync(() -> {
            Platform.runLater(() -> loadingIndicatorVisible.set(true));
            return fetchModInfo(modName);
        }).thenCompose(modInfo -> {
            // Maybe ask for a language (UI step)
            if (modInfo.isMultilingual()) {
                CompletableFuture<ContentLanguage> langFuture = new CompletableFuture<>();
                Platform.runLater(() -> {
                    ContentLanguage language = selectLanguage(modInfo);
                    if (language == null) {
                        language = ContentLanguage.DEFAULT_OR_RU;
                    }
                    langFuture.complete(language);
                });
                return langFuture.thenApply(lang -> fetchDownloadInfo(modInfo, lang));
            } else {
                return CompletableFuture.supplyAsync(() -> fetchDownloadInfo(modInfo, ContentLanguage.DEFAULT_OR_RU));
            }
        }).thenAccept(downloadInfo -> {
            // Validate before download
            if (!validateForDownload(downloadInfo)) {
                resetBrowser();
                return;
            }

            if (Files.exists(Path.of(getConfiguration().getDirectoryDownloads(), downloadInfo.fileName))) {
                CompletableFuture<Boolean> choiceFuture = new CompletableFuture<>();
                Platform.runLater(() -> {
                    String text = "File '" + downloadInfo.fileName + "' already exists on disk. Add to library?";
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.YES, ButtonType.NO);
                    alert.showAndWait();

                    choiceFuture.complete(alert.getResult() == ButtonType.YES);
                });
                choiceFuture.thenApply(choice -> {
                    resetBrowser();
                    if (choice) {
                        ContentModel contentModel = new ContentModel().fromModInfo(downloadInfo.modInfo);
                        contentModel.setDownloadedFile(new ContentFile(Path.of(getConfiguration().getDirectoryDownloads(), downloadInfo.fileName)));
                        contentModel.setDownloadedFileName(downloadInfo.fileName);
                        FileHandler.registerNewFile(contentModel, contentModel.getDownloadedFile().getAbsolutePath());
                        getModel().getContent().add(contentModel);
                    }
                    return null;
                });
            } else if (downloadInfo != null) {
                startDownload(downloadInfo);
            } else {
                resetBrowser();
            }
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                resetBrowser();
                App.showError((Exception) ex);
            });
            return null;
        });
    }

    private void resetBrowser() {
        loadingIndicatorVisible.set(false);
        Platform.runLater(() -> browser.get().load(lastLocation));
    }

    private ModInfo fetchModInfo(String modName) {
        try (Requester requester = new Requester()) {
            return requester.requestModInfo(modName);
        } catch (IOException | InterruptedException e) {
            log.severe(e.toString());
            App.showError(e);
            return null;
        }
    }

    private DownloadInfo fetchDownloadInfo(ModInfo modInfo, ContentLanguage lang) {
        try (Requester requester = new Requester()) {
            URI downloadURI = requester.requestDownloadURI(modInfo.getId(), lang);
            Requester.FileInfo fileInfo = requester.requestFileInfo(downloadURI);

            return new DownloadInfo(modInfo, downloadURI, fileInfo.getFileName());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void startDownload(DownloadInfo downloadInfo) {
        // Download file via task and add to queue
        Downloader downloader = new Downloader(downloadInfo.modInfo, downloadInfo.fileUri, TMP_DONWLOADS);
        downloader.setUpdateIntervalMillis(1000L);
        downloader.setOnSucceeded((workerStateEvent -> {
            try {
                ContentModel contentModel = downloader.get();
                if (Files.exists(Path.of(contentModel.getDownloadedFile().getAbsolutePath()))) {
                    FileHandler.registerNewFile(contentModel, contentModel.getDownloadedFile().getAbsolutePath());
                    getModel().getContent().add(contentModel);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            if (getConfiguration().isAutoClearFinishedDownloads()) {
                downloadTaskView.getTasks().remove(downloader);
            }
            activeDownloads.remove(downloadInfo.fileName);
        }));
        downloader.setOnFailed((workerStateEvent -> {
            log.severe(workerStateEvent.getSource().getException().toString());
            activeDownloads.remove(downloadInfo.fileName);
        }));
        activeDownloads.put(downloadInfo.fileName, downloader);
        Platform.runLater(() -> {
            downloadTaskView.getTasks().add(downloader);
            setStopDownloadButtonDisabled(false);

            resetBrowser();
        });

        executorService.submit(downloader);
    }

    /**
     * Check database or active downloads for the presence of the same mod via metadata before starting a download.
     *
     * @param downloadInfo
     * @return
     */
    private boolean validateForDownload(DownloadInfo downloadInfo) {
        // we don't support skins or resources for simplicity
        if (downloadInfo.modInfo.getType().ordinal() >= Type.SKIN.ordinal()) {
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Content type not supported: " + downloadInfo.modInfo.getType().getName()).show());
            return false;
        }

        if (activeDownloads.keySet().stream()
                .anyMatch(fileName -> fileName.equals(downloadInfo.fileName))) {
            log.warning("Content '" + downloadInfo.modInfo.getTitle() + "' already in download list.");
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "File already downloading!").show());
            return false;
        }

        // check existence in model
        ModInfo finalModInfo = downloadInfo.modInfo;
        if (getModel().getContent().stream().anyMatch(content ->
                content.getName().equals(finalModInfo.getTitle()))) {
            log.warning("Content '" + downloadInfo.modInfo.getTitle() + "' already in download list.");
            Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "File already in library!").show());
            return false;
        }

        return true;
    }

    /**
     * Shows language selector for maps/mods that have different downloads for different languages, since
     * we can't make out which button was pressed through WebView.
     *
     * @param modInfo
     * @return
     */
    private ContentLanguage selectLanguage(ModInfo modInfo) {
        ContentLanguage language;
        List<ContentLanguage> availableLanguages = modInfo.getLinks().stream()
                .map(ModInfo.Link::getType)
                .toList();
        ChoiceDialog<ContentLanguage> dlg = new ChoiceDialog<>();
        dlg.getItems().setAll(availableLanguages);
        dlg.setSelectedItem(dlg.getItems().get(0));
        dlg.initOwner(App.MAINSTAGE.getOwner());
        dlg.setTitle("Language Select");
        dlg.getDialogPane().setContentText("Select language:");
        dlg.setResultConverter((ButtonType type) -> {
            ButtonBar.ButtonData data = type == null ? null : type.getButtonData();
            if (data == ButtonBar.ButtonData.OK_DONE) {
                return dlg.getSelectedItem();
            } else {
                return null;
            }
        });
        Optional<ContentLanguage> result = dlg.showAndWait();
        if (result.isPresent()) {
            language = result.get();
        } else {
            browser.get().load(lastLocation);
            return null;
        }
        return language;
    }

    /**
     * Checks for the existance of all necessary paths before any download should be attempted.
     *
     * @return
     */
    private boolean checkPrerequisitesForDownload() {
        if (configuration.get().getDirectoryDownloads() == null
                || configuration.get().getDirectoryDownloads().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No download directory set!\nCheck your settings.").show();
            return false;
        }
        if (!Files.exists(Path.of(configuration.get().getDirectoryDownloads()))) {
            new Alert(Alert.AlertType.ERROR, "Download directory does not exist!\nCheck your settings.").show();
            return false;
        }

        // Check for temp download dir
        Path path = Path.of(TMP_DONWLOADS);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                App.showError(e);
                return false;
            }
        }
        return true;
    }

    @FXML
    public void onClearDownloadList() {
        for (Downloader downloader : activeDownloads.values().stream().filter(FutureTask::isDone).toList()) {
            downloadTaskView.getTasks().remove(downloader);
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
        for (Downloader downloader : activeDownloads.values()) {
            downloader.cancel(true);
        }
    }

    boolean pauseToggle = false;

    @FXML
    public void onPauseDownloads(ActionEvent event) {
        pauseToggle = !pauseToggle;
        for (var download : activeDownloads.values()) {
            download.setPause(pauseToggle);
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

    public boolean isLoadingIndicatorVisible() {
        return loadingIndicatorVisible.get();
    }

    public BooleanProperty loadingIndicatorVisibleProperty() {
        return loadingIndicatorVisible;
    }

    public void setLoadingIndicatorVisible(boolean loadingIndicatorVisible) {
        this.loadingIndicatorVisible.set(loadingIndicatorVisible);
    }
}
