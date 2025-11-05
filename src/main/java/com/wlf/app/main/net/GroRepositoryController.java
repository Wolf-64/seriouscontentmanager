package com.wlf.app.main.net;

import com.wlf.app.App;
import com.wlf.app.main.data.*;
import com.wlf.app.main.io.FileHandler;
import com.wlf.common.BaseController;
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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
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
    private ProgressBar downloadProgress;

    @FXML
    private TaskProgressView<Task<ContentModel>> downloadTaskView;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private final List<Downloader> activeDownloads = new ArrayList<>();

    private final BooleanProperty stopDownloadButtonDisabled = new SimpleBooleanProperty(true);

    private boolean downloadInProgress;

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
        if (configuration.get().getDirectoryDownloads() == null || configuration.get().getDirectoryDownloads().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No download directory set!\nCheck your settings.").show();
            browser.get().load(lastLocation);
            return;
        }
        if (!Files.exists(Path.of(configuration.get().getDirectoryDownloads()))) {
            new Alert(Alert.AlertType.ERROR, "Download directory does not exist!\nCheck your settings.").show();
            browser.get().load(lastLocation);
            return;
        }

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
        URI downloadURI = null;
        try (Requester requester = new Requester()) {
            modInfo = requester.requestModInfo(modName);
            if (modInfo.getLinks().size() > 1) {
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
                    return;
                }
            }
            downloadURI = requester.requestDownloadURI(modInfo.getId(), language);
        } catch (IOException | InterruptedException e) {
            App.showError(e);
            return;
        }

        // check for already active download of the same file
        ModInfo finalModInfo1 = modInfo;
        if (activeDownloads.stream()
                .anyMatch(downloader ->
                        downloader.getModInfo().getTitle().equals(finalModInfo1.getTitle())
                                && downloader.isRunning())) {
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

        downloadProgress.setProgress(0.0);

        // fire request and download file
        Downloader downloader = new Downloader(modInfo, downloadURI, TMP_DONWLOADS);
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

            if (activeDownloads.stream().allMatch(FutureTask::isDone)) {
                executorService.close();
            }
        }));
        downloader.setOnFailed((workerStateEvent -> {
            log.severe(workerStateEvent.getSource().getException().toString());
        }));
        activeDownloads.add(downloader);
        downloadTaskView.getTasks().add(downloader);
        executorService.submit(downloader);

        downloadInProgress = true;
        setStopDownloadButtonDisabled(false);

        browser.get().load(lastLocation);
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
        for (Downloader downloader : activeDownloads) {
            downloader.cancel(true);
        }
    }

    boolean pauseToggle = false;

    @FXML
    public void onPauseDownloads(ActionEvent event) {
        pauseToggle = !pauseToggle;
        for (var download : activeDownloads) {
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
}
