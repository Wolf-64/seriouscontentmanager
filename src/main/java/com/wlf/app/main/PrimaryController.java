package com.wlf.app.main;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wlf.app.preferences.Config;
import com.wlf.common.BaseController;
import com.wlf.common.BaseModel;
import com.wlf.common.controls.AccentedProgressBar;
import com.wlf.app.main.data.*;
import com.wlf.app.main.io.FileHandler;
import com.wlf.app.main.io.GameHandler;
import com.wlf.app.main.net.Downloader;

import com.wlf.app.main.net.Requester;
import com.wlf.app.main.util.LocalDateTimeStringConverter;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.exec.DefaultExecuteResultHandler;

public class PrimaryController extends BaseController<BaseModel> {
    static Logger log = Logger.getLogger(PrimaryController.class.getSimpleName());

    @FXML
    private final ObjectProperty<Config> config = new SimpleObjectProperty<>(Config.getInstance());

    @FXML
    private final ObjectProperty<Filter> tableFilter = new SimpleObjectProperty<>(new Filter());

    @FXML @Getter @Setter
    WebView webView;

    private final ObjectProperty<WebEngine> browser = new SimpleObjectProperty<>();

    @FXML TabPane tabPane;

    @FXML ProgressBar downloadProgress;

    // --- Settings ---
    @FXML TextField tfDirectoryDownloads;
    @FXML TextField tfDirectoryTFE;
    @FXML TextField tfDirectoryTSE;

    @FXML ImageView imgCheckTFE, imgCheckTSE;

    @FXML Button btnBrowseDirectoryDownloads;
    @FXML Button btnBrowseDirectoryTFE;
    @FXML Button btnBrowseDirectoryTSE;
    @FXML Button btnSaveConfig;

    // --- Filter ---
    @FXML TextField tfNameFilter;
    @FXML RadioButton rbTfe, rbTse, rbMaps, rbMods, rbSp, rbCoop, rbDm;
    @FXML CheckBox cbInstalled, cbCompleted;

    @FXML ToggleGroup tgGame, tgType, tgMode;
    @FXML TableView<ContentModel> table;
    @FXML TableColumn<ContentModel, Void> actionColumn;
    @FXML TableColumn<ContentModel, LocalDateTime> colDateAdded;
    @FXML TableColumn<ContentModel, String> colDateCreated;

    // --- TableView context menu ---
    @FXML MenuItem menuItemInstall;
    @FXML MenuItem menuItemRemove;

    private final DBManager dbManager = DBManager.getInstance();

    private final String GRO_REPOSITORY_URL = "https://grorepository.ru/mods";
    private final String GRO_MODPAGE_URL = "https://grorepository.ru/mod/";
    private final String ABOUT_BLANK = "about:blank";
    private final String TMP_DONWLOADS = "tmpDownload";

    @Setter @Getter
    private ObservableList<ContentModel> fileEntries = FXCollections.observableArrayList();

    @Getter @Setter
    private ObservableList<DownloadModel> activeDonwloads = FXCollections.observableArrayList();

    private final BooleanProperty stopDownloadButtonDisabled = new SimpleBooleanProperty(true);
    private final BooleanProperty tfeLocationValid = new SimpleBooleanProperty();
    private final BooleanProperty tseLocationValid = new SimpleBooleanProperty();
    private final BooleanProperty installDisabled = new SimpleBooleanProperty();
    private final BooleanProperty installVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty installMultiDisabled = new SimpleBooleanProperty();
    private final BooleanProperty installMultiVisible = new SimpleBooleanProperty();
    private final BooleanProperty removeDisabled = new SimpleBooleanProperty();
    private final BooleanProperty removeVisible = new SimpleBooleanProperty(true);

    private boolean downloadInProgress;

    private String lastLocation;

    private final ObjectProperty<ContentModel> currentSelection = new SimpleObjectProperty<>();

    @FXML
    public void initialize() {
        // register WebView lazy-load when tab active
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal.intValue() == 1 // should always be second tab
                    && browser.get().getHistory().getEntries().isEmpty()) { // only if not yet opened
                browser.get().load(GRO_REPOSITORY_URL);
            }
        });

        initBindings();

        // load existing entries from DB
        fileEntries.addAll(dbManager.getAllFileEntries());
        fileEntries.forEach(item ->
                item.completedProperty().addListener(getListItemListener(item)));

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
            } catch (NoSuchElementException ignored) {}
        }));

        tfDirectoryDownloads.setText(config.get().getDirectoryDownloads());
        tfDirectoryTFE.setText(config.get().getDirectoryTFE());
        tfDirectoryTSE.setText(config.get().getDirectoryTSE());

        tfDirectoryTFE.textProperty().addListener((observable, oldVal, newVal) -> {
            // validate and set...
            validateTFEPath(newVal);
        });
        tfDirectoryTSE.textProperty().addListener((observable, oldVal, newVal) -> {
            validateTSEPath(newVal);
        });
        tfDirectoryDownloads.textProperty().addListener((observable, oldVal, newVal) -> {
            config.get().setDirectoryDownloads(newVal);
        });

        validateGamePaths();

        // test stuff TODO remove
        /*
        File testfolder = new File("C:\\Users\\Wolf\\Downloads\\SSTSE\\Test");
        ArrayList<ContentModel> foundStuff = new ArrayList();
        Arrays.stream(testfolder.listFiles()).forEach(file -> {
            try {
                if (file.getAbsolutePath().toLowerCase().endsWith(".gro")) {
                    GroFile gro = new GroFile(file.getAbsolutePath());
                    foundStuff.add(gro.analyzeContent());
                } else {
                    ZipFile zip = new ZipFile(file.getAbsolutePath());
                    foundStuff.add(zip.analyzeContent());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        GroFile test = new GroFile("C:\\Users\\Wolf\\Downloads\\SSTSE\\GR.gro");
        var model = test.analyzeContent();
        model.canInstall();

         */

        colDateAdded.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateTimeStringConverter()));
    }

    private void onDownloadRequestReceived() {
        String modName = lastLocation.substring(lastLocation.lastIndexOf('/') + 1);
        DownloadModel downloadModel = new DownloadModel();
        downloadModel.setFileName(modName);
        downloadModel.setDownloadURL(lastLocation);

        // check for already active download of the same file
        if (activeDonwloads.stream().anyMatch(download -> download.getDownloadURL().equals(lastLocation))) {
            log.warning("Content '" + modName + "' already in download list.");
            new Alert(AlertType.WARNING, "File already downloading!").show();
            browser.get().load(lastLocation);
            //onBrowserBack(null);
            return;
        }

        // check for already existing file on system
        try (Requester requester = new Requester()) {
            ContentModel mod = new ContentModel().fromJSON(requester.requestModInfo(modName));
            if (fileEntries.stream().anyMatch(item -> item.getName().equals(mod.getName()))) {
                new Alert(AlertType.WARNING, "Content already in library!").show();
                browser.get().load(lastLocation);
                //onBrowserBack(null);
                return;
            }
            // we don't support skins or resources for simplicity
            if (mod.getType().ordinal() >= Type.SKIN.ordinal()) {
                new Alert(AlertType.WARNING, "Content type not supported: " + mod.getType().getName()).show();
                browser.get().load(lastLocation);
                return;
            }
        } catch (IOException | InterruptedException e) {
            // show message?
            return;
        }

        activeDonwloads.add(downloadModel);
        downloadProgress.setProgress(0.0);

        // Check for temp download dir
        Path path = Path.of(TMP_DONWLOADS);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                log.severe(e.toString());
            }
        }

        // fire request and download file
        Downloader downloader = new Downloader();
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
        downloader.download(modName, TMP_DONWLOADS);

        downloadInProgress = true;
        setStopDownloadButtonDisabled(false);

        browser.get().load(lastLocation);
        //onBrowserBack(null);
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
        }
        setStopDownloadButtonDisabled(true);
        // is null when download has been cancelled
        if (filePath != null) {
            FileHandler.registerNewFile(downloadedContentModel, filePath);
            downloadedContentModel.completedProperty().addListener(getListItemListener(downloadedContentModel));
            fileEntries.add(downloadedContentModel);
        }
    }

    private InvalidationListener getListItemListener(ContentModel object) {
        return observable -> dbManager.update(object);
    }


    @FXML
    public void rescanDownloadDir(ActionEvent event) {
        // TODO
    }

    @FXML
    public void onPlayTFE() {
        try {
            if (false /*config.get().isUseSteamRuntime()*/) {
                GameHandler.startGameWithSteam(Game.TFE);
            } else {
                GameHandler.startGameExe(Game.TFE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onPlayTSE() {
        try {
            if (false /*config.get().isUseSteamRuntime()*/) {
                GameHandler.startGameWithSteam(Game.TSE);
            } else {
                GameHandler.startGameExe(Game.TSE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public String browseForDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null && selectedDirectory.exists() && selectedDirectory.isDirectory()) {
            return selectedDirectory.getAbsolutePath();
        } else {
            return null;
        }
    }

    @FXML
    public void validateDirectory(String path) {

    }

    @FXML
    public void onBrowserBack(ActionEvent event) {
        try {
            browser.get().getHistory().go(-1);
        } catch (IndexOutOfBoundsException ignored) {}
    }

    @FXML
    public void onBrowserForward(ActionEvent event) {
        try {
            browser.get().getHistory().go(1);
        } catch (IndexOutOfBoundsException ignored) {}
    }

    @FXML
    public void onBrowserRefresh(ActionEvent event) {
        browser.get().reload();
    }

    @FXML
    public void onAbout(ActionEvent event) {
        // TODO move to template
        //App.showAbout();
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

    @FXML
    public void onDeployTFE(ActionEvent event) {
        deploy(Game.TFE);
    }

    @FXML
    public void onDeployTSE(ActionEvent event) {
        deploy(Game.TSE);
    }

    @FXML
    public void onDeploy(ActionEvent event) {
        deploy(currentSelection.get().getGame());
    }

    private void deploy(Game game) {
        String modPath = config.get().getDirectoryDownloads() + "/" + currentSelection.get().getDownloadedFileName();
        log.log(Level.INFO, "Deploying {0}...", modPath);
        File mod = new File(modPath);
        if (mod.exists()) {
            // check file type
            if (currentSelection.get().isGro()) {
                Path installPath = Path.of(game.getGameFolder() + "/" + currentSelection.get().getDownloadedFileName());
                log.log(Level.INFO, "...to {0}", installPath);

                FileHandler.installContent(currentSelection.get());
                currentSelection.get().setInstalled(true);
                dbManager.update(currentSelection.get());
            }
        }
    }

    @FXML
    public void onPlaySingleMap(ActionEvent event) {
        FileHandler.createTempMod(currentSelection.get());
        try {
            Task<Integer> task = new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    log.info("Staring game...");
                    DefaultExecuteResultHandler resultHandler = null;
                    if (false /*config.get().isUseSteamRuntime()*/) {
                        resultHandler = GameHandler.startGameWithSteam(currentSelection.get().getGame());
                    } else {
                        resultHandler = GameHandler.startGameExe(currentSelection.get().getGame());
                    }
                    resultHandler.waitFor();
                    return resultHandler.getExitValue();
                }
            };
            task.setOnFailed((workerStateEvent) -> {
                log.severe(workerStateEvent.getSource().toString());
            });
            task.setOnSucceeded((workerStateEvent) -> {
                try {
                    // this exit code standard?
                    if (task.get() == -559038737) {

                    }
                    log.info("Game has been quit. (Exit code " + task.get() + ")");
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                FileHandler.removeTempMod(currentSelection.get());
            });

            new Thread(task).start();
        } catch (Exception e) {
            log.severe(e.toString());
        }
    }

    @FXML
    public void onRemoveTFE(ActionEvent event) {
        remove(Game.TFE);
    }

    @FXML
    public void onRemoveTSE(ActionEvent event) {
        remove(Game.TSE);
    }

    @FXML
    public void onRemove(ActionEvent event) {
        remove(currentSelection.get().getGame());
    }

    private void remove(Game game) {
        String modPath = game.getGameFolder() + "/" + currentSelection.get().getDownloadedFileName();
        log.log(Level.INFO, "Removing {0}...", modPath);
        File mod = new File(modPath);
        if (mod.exists()) {
            // check file type
            if (currentSelection.get().isGro()) {
                FileHandler.removeContent(currentSelection.get());
            }
        }

        currentSelection.get().setInstalled(false);
        dbManager.update(currentSelection.get());
    }

    @FXML
    public void onMarkComplete(ActionEvent actionEvent) {
        currentSelection.get().setCompleted(!currentSelection.get().isCompleted());
        dbManager.update(currentSelection.get());
    }

    @FXML
    public void onRate(ActionEvent actionEvent) {
    }

    @FXML
    public void onDelete(ActionEvent actionEvent) {
        FileHandler.deleteContent(currentSelection.get());
        fileEntries.remove(currentSelection.get());
    }

    @FXML
    public void updateContextMenu(WindowEvent windowEvent) {
        menuItemInstall.setDisable(true);
        menuItemRemove.setDisable(true);

        if (currentSelection.get() != null) {
            ContentModel selection = currentSelection.get();
            menuItemInstall.setDisable(!selection.canInstall());
            menuItemRemove.setDisable(!selection.canRemove());
        }
    }

    @FXML
    public void onSaveSettings(ActionEvent event) {
        validateGamePaths();
        try {
            Config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onDirectoryDrop(DragEvent event) {
        if (event.getSource() instanceof TextField tf) {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                tf.setText(db.getFiles().getFirst().getAbsolutePath());
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
        }
        event.consume();
    }

    @FXML
    public void onDirectoryDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles() && event.getDragboard().getFiles().size() == 1) {
            event.acceptTransferModes(TransferMode.ANY);
        }

        event.consume();
    }

    private void initBindings() {
        // register directory browser buttons
        btnBrowseDirectoryDownloads.setOnAction((event) -> {
            String path = browseForDirectory();
            config.get().setDirectoryDownloads(path);
            onSaveSettings(event);
        });
        btnBrowseDirectoryTFE.setOnAction((event) -> {
            String path = browseForDirectory();
            setGamePath(path, Game.TFE);
            onSaveSettings(event);
        });
        btnBrowseDirectoryTSE.setOnAction((event) -> {
            String path = browseForDirectory();
            setGamePath(path, Game.TSE);
            onSaveSettings(event);
        });

        getTableFilter().setGameSelected(((Game)tgGame.getSelectedToggle().getUserData()).ordinal());
        getTableFilter().setTypeSelected(((Type)tgType.getSelectedToggle().getUserData()).ordinal());
        getTableFilter().setModeSelected(((Mode)tgMode.getSelectedToggle().getUserData()).ordinal());
        cbInstalled.selectedProperty().bindBidirectional(getTableFilter().installedProperty());
        cbCompleted.selectedProperty().bindBidirectional(getTableFilter().completedProperty());
        tfNameFilter.textProperty().bindBidirectional(getTableFilter().nameProperty());

        tgGame.selectedToggleProperty().addListener((property, oldVal, newVal) -> {
            getTableFilter().setGameSelected(((Game)newVal.getUserData()).ordinal());
        });
        tgMode.selectedToggleProperty().addListener((property, oldVal, newVal) -> {
            getTableFilter().setModeSelected(((Mode)newVal.getUserData()).ordinal());
        });
        tgType.selectedToggleProperty().addListener((property, oldVal, newVal) -> {
            getTableFilter().setTypeSelected(((Type)newVal.getUserData()).ordinal());
        });

        getTableFilter().gameSelectedProperty().addListener(filterListener);
        getTableFilter().typeSelectedProperty().addListener(filterListener);
        getTableFilter().modeSelectedProperty().addListener(filterListener);
        getTableFilter().installedProperty().addListener(filterListener);
        getTableFilter().completedProperty().addListener(filterListener);
        getTableFilter().nameProperty().addListener(filterListener);

        table.getSelectionModel().selectedItemProperty().addListener(((observable, oldVal, newVal) -> {
            currentSelection.set(newVal);
        }));

        // deploy function in button column or per right/double click?
        /*
        actionColumn.setCellFactory(param -> {
            ButtonCellFactory<FileEntry> factory = new ButtonCellFactory<>();
            factory.setButtonText("▶\uFE0F");
            factory.setActionConsumer(item -> {
                log.info(item.getName());
            });

            return factory.call(param);
        });
         */
    }

    private void validateTFEPath(String path) {
        boolean valid = Game.TFE.isGamePathValid(path);
        config.get().setTfeDirectoryValid(valid); // mark text if not valid
        imgCheckTFE.setVisible(valid);

        if (valid) {
            config.get().setDirectoryTFE(path);
        }
    }

    private void validateTSEPath(String path) {
        boolean valid = Game.TSE.isGamePathValid(path);
        config.get().setTseDirectoryValid(valid);
        imgCheckTSE.setVisible(valid);

        if (valid) {
            config.get().setDirectoryTSE(path);
        }
    }

    private void validateGamePaths() {
        validateTFEPath(tfDirectoryTFE.getText());
        validateTSEPath(tfDirectoryTSE.getText());
    }

    private void setGamePath(String path, Game game) {
        if (Game.TFE.equals(game)) {
            config.get().setDirectoryTFE(path);
        } else if (Game.TSE.equals(game)) {
            config.get().setDirectoryTSE(path);
        }
    }

    ChangeListener<Object> filterListener = ((observable, oldVal, newVal) -> {
        applyFilter();
    });

    private void applyFilter() {
        fileEntries.setAll(dbManager.getFileEntries(getTableFilter()));
    }

    public Config getConfig() {
        return config.get();
    }

    public void setConfig(Config config) {
        this.config.set(config);
    }

    public ObjectProperty<Config> configProperty() {
        return config;
    }

    public Filter getTableFilter() {
        return tableFilter.get();
    }

    public ObjectProperty<Filter> tableFilterProperty() {
        return tableFilter;
    }

    public void setTableFilter(Filter tableFilter) {
        this.tableFilter.set(tableFilter);
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

    public boolean isInstallDisabled() {
        return installDisabled.get();
    }

    public BooleanProperty installDisabledProperty() {
        return installDisabled;
    }

    public void setInstallDisabled(boolean installDisabled) {
        this.installDisabled.set(installDisabled);
    }

    public boolean isRemoveDisabled() {
        return removeDisabled.get();
    }

    public BooleanProperty removeDisabledProperty() {
        return removeDisabled;
    }

    public void setRemoveDisabled(boolean removeDisabled) {
        this.removeDisabled.set(removeDisabled);
    }

    public boolean isTfeLocationValid() {
        return tfeLocationValid.get();
    }

    public BooleanProperty tfeLocationValidProperty() {
        return tfeLocationValid;
    }

    public boolean isTseLocationValid() {
        return tseLocationValid.get();
    }

    public BooleanProperty tseLocationValidProperty() {
        return tseLocationValid;
    }

    public boolean isInstallVisible() {
        return installVisible.get();
    }

    public BooleanProperty installVisibleProperty() {
        return installVisible;
    }

    public boolean isRemoveVisible() {
        return removeVisible.get();
    }

    public BooleanProperty removeVisibleProperty() {
        return removeVisible;
    }

    public boolean isInstallMultiDisabled() {
        return installMultiDisabled.get();
    }

    public BooleanProperty installMultiDisabledProperty() {
        return installMultiDisabled;
    }

    public boolean isInstallMultiVisible() {
        return installMultiVisible.get();
    }

    public BooleanProperty installMultiVisibleProperty() {
        return installMultiVisible;
    }

    public ContentModel getCurrentSelection() {
        return currentSelection.get();
    }

    public ObjectProperty<ContentModel> currentSelectionProperty() {
        return currentSelection;
    }

    public WebEngine getBrowser() {
        return browser.get();
    }

    public ObjectProperty<WebEngine> browserProperty() {
        return browser;
    }

    public void setBrowser(WebEngine browser) {
        this.browser.set(browser);
    }
}
