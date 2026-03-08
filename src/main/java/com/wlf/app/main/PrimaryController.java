package com.wlf.app.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import com.wlf.app.App;
import com.wlf.app.AppLoader;
import com.wlf.app.main.net.GroRepositoryController;
import com.wlf.app.preferences.Config;
import com.wlf.app.preferences.PreferencesController;
import com.wlf.common.BaseController;
import com.wlf.app.main.data.*;
import com.wlf.app.main.io.FileHandler;
import com.wlf.app.main.io.GameHandler;

import com.wlf.common.util.FileDialogues;
import com.wlf.common.util.LocalDateConverter;
import com.wlf.common.util.LocalDateTimeConverter;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.controlsfx.control.StatusBar;
import org.slf4j.event.Level;

@Slf4j
public class PrimaryController extends BaseController<DataModel> {
    @FXML
    private final ObjectProperty<Config> config = new SimpleObjectProperty<>(Config.getInstance());

    @FXML
    private final ObjectProperty<TableFilter> tableFilter = new SimpleObjectProperty<>(new TableFilter());

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab settings, grorepo;

    // --- Filter ---
    @FXML
    private TextField tfNameFilter;
    @FXML
    private CheckBox cbInstalled, cbCompleted;

    @FXML
    private ToggleGroup tgGame, tgType, tgMode;
    @FXML
    private TableView<ContentModel> table;
    @FXML
    private TableColumn<ContentModel, Void> actionColumn;
    @FXML
    private TableColumn<ContentModel, LocalDateTime> colDateAdded, colDateLastPlayed, colDateCompleted;
    @FXML
    private TableColumn<ContentModel, LocalDate> colDateCreated;
    @FXML
    private DatePicker dateFrom, dateTo;

    // --- TableView context menu ---
    @FXML
    private MenuItem menuItemInstall;
    @FXML
    private MenuItem menuItemRemove;

    @FXML
    private StatusBar statusBar;

    private GroRepositoryController repoController;

    private final BooleanProperty installDisabled = new SimpleBooleanProperty();
    private final BooleanProperty installVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty installMultiDisabled = new SimpleBooleanProperty();
    private final BooleanProperty installMultiVisible = new SimpleBooleanProperty();
    private final BooleanProperty removeDisabled = new SimpleBooleanProperty();
    private final BooleanProperty removeVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty loadingIndicatorVisible = new SimpleBooleanProperty(false);

    private final ObjectProperty<ContentModel> currentSelection = new SimpleObjectProperty<>();
    ChangeListener<Object> filterListener = ((observable, oldVal, newVal) -> {
        applyFilter();
    });


    @FXML
    public void initialize() {
        AppLoader<PreferencesController> loaderSettings = new AppLoader<>("preferences.fxml", (gui) -> {
            settings.setContent(gui);
        });
        loaderSettings.loadAsync();

        AppLoader<GroRepositoryController> loaderGroRepo = new AppLoader<>("main/repositoryView.fxml", (gui) -> {
            grorepo.setContent(gui);
        });
        loaderGroRepo.setOnSucceeded((controller) -> {
            repoController = controller;
            repoController.setModel(getModel());
            // defer instantiation of WebView after render
            Platform.runLater(() -> {
                Platform.runLater(repoController::initWebView);
            });
        });
        loaderGroRepo.loadAsync();

        // register WebView lazy-load when tab active
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (repoController.getBrowser().getHistory().getEntries().isEmpty()) {
                repoController.loadGroRepo();
            }
        });

        initBindings();

        // load existing entries from DB
        setModel(new DataModel());

        for (ContentModel contentModel : ContentRepository.getInstance().findAll()) {
            contentModel.setupListener();
            getModel().getContent().add(contentModel);
        }

        colDateCreated.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateConverter()));
        colDateAdded.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateTimeConverter()));
        colDateCompleted.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateTimeConverter()));
        colDateLastPlayed.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateTimeConverter()));

        // store column configuration on app close
        getOnCloseRequestCallbacks().add((windowEvent -> {
            for(int i = 0; i < table.getColumns().size(); i++) {
                getConfig().getTableColumnHeaders()[i] = table.getColumns().get(i).isVisible();
            }
            try {
                Config.save();
            } catch (IOException e) {
                log.error(e.toString());
            }
        }));

        // restore column header visibilities
        for(int i = 0; i < table.getColumns().size(); i++) {
            table.getColumns().get(i).setVisible(getConfig().getTableColumnHeaders()[i]);
        }
    }

    private void initBindings() {
        getTableFilter().setGame(((Game) tgGame.getSelectedToggle().getUserData()));
        getTableFilter().setType(((Type) tgType.getSelectedToggle().getUserData()));
        getTableFilter().setModes(((Mode) tgMode.getSelectedToggle().getUserData()));
        cbInstalled.selectedProperty().bindBidirectional(getTableFilter().installedProperty());
        cbCompleted.selectedProperty().bindBidirectional(getTableFilter().completedProperty());
        tfNameFilter.setOnAction((evt) -> applyFilter());
        tfNameFilter.focusedProperty().addListener(((observableValue, oldValue, newValue) -> {
            if (!newValue) {
                applyFilter();
            }
        }));
        tfNameFilter.textProperty().bindBidirectional(getTableFilter().nameProperty());

        tgGame.selectedToggleProperty().addListener((property, oldVal, newVal) -> {
            getTableFilter().setGame((Game) newVal.getUserData());
        });
        tgMode.selectedToggleProperty().addListener((property, oldVal, newVal) -> {
            getTableFilter().setModes((Mode) newVal.getUserData());
        });
        tgType.selectedToggleProperty().addListener((property, oldVal, newVal) -> {
            getTableFilter().setType((Type) newVal.getUserData());
        });

        getTableFilter().gameProperty().addListener(filterListener);
        getTableFilter().typeProperty().addListener(filterListener);
        getTableFilter().modesProperty().addListener(filterListener);
        getTableFilter().installedProperty().addListener(filterListener);
        getTableFilter().completedProperty().addListener(filterListener);

        dateFrom.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            getTableFilter().setDateCreatedFrom(newVal);
            applyFilter();
        });
        dateTo.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            getTableFilter().setDateCreatedTo(newVal);
            applyFilter();
        });

        table.getSelectionModel().selectedItemProperty().addListener(((observable, oldVal, newVal) -> {
            currentSelection.set(newVal);
        }));
    }

    @FXML
    public void onRescanDownloadDir(ActionEvent event) {
        loadingIndicatorVisible.set(true);
        if (Files.isDirectory(Path.of(getConfig().getDirectoryDownloads()))) {
            File[] files = Path.of(getConfig().getDirectoryDownloads()).toFile().listFiles();
            if (files != null) {
                Task<Void> scanTask = new Task<>() {
                    @Override
                    protected Void call() {
                        for (int i = 0; i < files.length; i++) {
                            File downloadedFile = files[i];
                            int progress = i;
                            Platform.runLater(() -> setStatusBarContent("Scanning " + downloadedFile.getName() + "...", (double) files.length / progress));
                            if (downloadedFile.getName().endsWith(".gro") || downloadedFile.getName().endsWith(".zip")) {
                                importFile(downloadedFile.toPath());
                            }
                        }

                        return null;
                    }
                };
                scanTask.setOnScheduled((_) -> statusBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS));
                scanTask.setOnSucceeded((_) -> {
                    resetStatusBar();
                    loadingIndicatorVisible.set(false);
                });
                scanTask.setOnFailed((wse) -> setStatusBarContent("Error scanning for files! " + wse.getSource().getException().getLocalizedMessage()));
                new Thread(scanTask).start();
            }
        }
    }

    @FXML
    public void onImportFile(ActionEvent event) {
        File file = FileDialogues.chooseFile(Path.of(System.getProperty("user.home"))
                , new FileChooser.ExtensionFilter("All files",  "*.*")
                , new FileChooser.ExtensionFilter("Group files", "*.gro")
                , new FileChooser.ExtensionFilter("ZIP archives", "*.zip"));

        if (file != null) {
            importFile(file.toPath());
        }
    }

    @FXML
    public void onPlayTFE() {
        runGame(Game.TFE);
    }

    @FXML
    public void onPlayTSE() {
        runGame(Game.TSE);
    }

    @FXML
    public void onAbout(ActionEvent event) {
        App.showAboutDialog();
    }

    public void onClose(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    public void onDeploy(ActionEvent event) {
        deploy(currentSelection.get());
    }

    private boolean checkGamePathValidity(Game game) {
        String gamePath = GameHandler.getGamePath(game);
        if (!game.isGamePathValid(gamePath)) {
            String text = "Path '" + gamePath + "' is not valid. Check your settings.";
            Alert alert = new Alert(Alert.AlertType.WARNING, text, ButtonType.OK);
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private void deploy(ContentModel model) {
        if (!checkFileExistence(model) || !checkGamePathValidity(model.getGame())) {
            return;
        }

        log.atLevel(Level.INFO).log("Deploying {0}...", model.getDownloadedFile());
        ;
        if (Files.exists(model.getDownloadedFile().toPath())) {
            // check file type
            if (model.isGro()) {
                Path installPath = Path.of(model.getGame().getGameFolder() + "/" + model.getDownloadedFileName());
                log.atLevel(Level.INFO).log("...to {0}", installPath);

                FileHandler.installContent(model);
                model.setInstalled(true);
                ContentRepository.getInstance().update(model);
            }
        }
    }

    @FXML
    public void onPlaySingleMap(ActionEvent event) {
        ContentModel model = currentSelection.get();

        // handle file not existent
        if (!checkFileExistence(model) || !checkGamePathValidity(model.getGame())) {
            return;
        }

        log.info("Staring game...");
        setStatusBarContent("Starting " + model.getGame().getName() + " for " + model.getName());
        loadingIndicatorVisible.set(true);
        try {
            Task<Integer> task = new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    DefaultExecuteResultHandler resultHandler = null;
                    if (model.getType() == Type.MOD) {
                        FileHandler.installMod(model);
                        resultHandler = GameHandler.startGame(model.getGame(), model.getDownloadedFile().findModName());
                    } else if (model.getType() == Type.MAP) {
                        FileHandler.createTempMod(model);
                        resultHandler = GameHandler.startGame(model.getGame());
                    }
                    resultHandler.waitFor();
                    if (resultHandler.getException() != null && resultHandler.getExitValue() != 0) {
                        throw resultHandler.getException();
                    }
                    return resultHandler.getExitValue();
                }
            };
            task.setOnFailed((workerStateEvent) -> {
                log.error(workerStateEvent.getSource().toString());
                App.showError(new Exception(workerStateEvent.getSource().getException()));
                resetStatusBar();
                loadingIndicatorVisible.set(false);
            });
            task.setOnSucceeded((workerStateEvent) -> {
                try {
                    // this exit code has not been set, just a placeholder, ignore?
                    if (task.get() == -559038737) {

                    }
                    log.info("Game has been quit. (Exit code " + task.get() + ")");
                    resetStatusBar();
                    loadingIndicatorVisible.set(false);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                if (model.getType() == Type.MAP) {
                    FileHandler.removeTempMod(model);
                } else if (model.getType() == Type.MOD) {
                    FileHandler.removeModContent(model);
                }
            });

            new Thread(task).start();
        } catch (Exception e) {
            log.error(e.toString());
            App.showError(e);
        }
    }

    private boolean checkFileExistence(ContentModel model) {
        if (!model.getDownloadedFile().exists()) {
            String text = "File '" + model.getDownloadedFile().getName() + "' does not exists on disk. Remove from library?";
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                delete(model);
            }

            return false;
        }
        return true;
    }

    @FXML
    public void onRemove(ActionEvent event) {
        remove(currentSelection.get());
    }

    private void remove(ContentModel model) {
        String modPath = model.getGame().getGameFolder() + "/" + model.getDownloadedFileName();
        log.atLevel(Level.INFO).log("Removing {0}...", modPath);
        File mod = new File(modPath);
        if (mod.exists()) {
            // check file type
            if (model.isGro()) {
                FileHandler.removeContent(model);
            }
        }

        currentSelection.get().setInstalled(false);
        ContentRepository.getInstance().update(currentSelection.get());
    }

    @FXML
    public void onMarkComplete(ActionEvent actionEvent) {
        currentSelection.get().setCompleted(!currentSelection.get().isCompleted());
        ContentRepository.getInstance().update(currentSelection.get());
    }

    @FXML
    public void onRate(ActionEvent actionEvent) {
    }

    @FXML
    public void onDelete(ActionEvent actionEvent) {
        delete(currentSelection.get());
    }

    private void delete(ContentModel model) {
        FileHandler.deleteContent(model);
        getModel().getContent().remove(model);
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

    private void runGame(Game game) {
        if (!checkGamePathValidity(game)) {
            return;
        }

        try {
            GameHandler.startGame(game);
        } catch (IOException e) {
            App.showError(e);
        }
    }

    private void resetStatusBar() {
        setStatusBarContent("Ready");
    }

    private void setStatusBarContent(String text) {
        setStatusBarContent(text, 0);
    }

    private void setStatusBarContent(String text, double progress) {
        statusBar.setText(text);
        statusBar.setProgress(progress);
    }

    private void importFile(Path file) {
        ContentFile contentFile = new ContentFile(file);
        try {
            ContentModel model = contentFile.analyzeContent();
            // check DB for present maps/mods
            ContentEntity entity = ContentRepository.getInstance().findByFileName(file.getFileName().toString());
            // only add it if it's not already in the DB
            if (entity == null) {
                FileHandler.registerNewFile(model, model.getDownloadedFile().getAbsolutePath());
                getModel().getContent().add(model);
            }
        } catch (IOException e) {
            log.error("Error analyzing file content during import.", e);
            App.showError(e);
        }
    }

    private void applyFilter() {
        getModel().getContent().setAll(ContentRepository.getInstance().filterByExample(tableFilter.get()));
        //getModel().getContent().setAll(dbManager.getFileEntries(getTableFilter()));
    }

    // ------------------------ FX Boilerplate ------------------------

    public Config getConfig() {
        return config.get();
    }

    public void setConfig(Config config) {
        this.config.set(config);
    }

    public ObjectProperty<Config> configProperty() {
        return config;
    }

    public TableFilter getTableFilter() {
        return tableFilter.get();
    }

    public ObjectProperty<TableFilter> tableFilterProperty() {
        return tableFilter;
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
