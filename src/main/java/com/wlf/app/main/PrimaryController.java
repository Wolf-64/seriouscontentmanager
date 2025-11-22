package com.wlf.app.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wlf.app.App;
import com.wlf.app.AppLoader;
import com.wlf.app.main.net.GroRepositoryController;
import com.wlf.app.preferences.Config;
import com.wlf.app.preferences.PreferencesController;
import com.wlf.common.BaseController;
import com.wlf.app.main.data.*;
import com.wlf.app.main.io.FileHandler;
import com.wlf.app.main.io.GameHandler;

import com.wlf.common.util.LocalDateConverter;
import com.wlf.common.util.LocalDateTimeConverter;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.WindowEvent;
import org.apache.commons.exec.DefaultExecuteResultHandler;

public class PrimaryController extends BaseController<DataModel> {
    static Logger log = Logger.getLogger(PrimaryController.class.getSimpleName());

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
    private RadioButton rbTfe, rbTse, rbMaps, rbMods, rbSp, rbCoop, rbDm;
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

    // --- TableView context menu ---
    @FXML
    private MenuItem menuItemInstall;
    @FXML
    private MenuItem menuItemRemove;

    private GroRepositoryController repoController;

    private final BooleanProperty installDisabled = new SimpleBooleanProperty();
    private final BooleanProperty installVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty installMultiDisabled = new SimpleBooleanProperty();
    private final BooleanProperty installMultiVisible = new SimpleBooleanProperty();
    private final BooleanProperty removeDisabled = new SimpleBooleanProperty();
    private final BooleanProperty removeVisible = new SimpleBooleanProperty(true);

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


    private InvalidationListener getListItemListener(ContentModel object) {
        return observable -> ContentRepository.getInstance().update(object);
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
    public void onAbout(ActionEvent event) {
        // TODO move to template
        //App.showAbout();
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
        Path modPath = Path.of(config.get().getDirectoryDownloads(), currentSelection.get().getDownloadedFileName());
        log.log(Level.INFO, "Deploying {0}...", modPath);
        ;
        if (Files.exists(modPath)) {
            // check file type
            if (currentSelection.get().isGro()) {
                Path installPath = Path.of(game.getGameFolder() + "/" + currentSelection.get().getDownloadedFileName());
                log.log(Level.INFO, "...to {0}", installPath);

                FileHandler.installContent(currentSelection.get());
                currentSelection.get().setInstalled(true);
                ContentRepository.getInstance().update(currentSelection.get());
            }
        }
    }

    @FXML
    public void onPlaySingleMap(ActionEvent event) {
        try {
            FileHandler.createTempMod(currentSelection.get());
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
            App.showError(e);
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
        FileHandler.deleteContent(currentSelection.get());
        getModel().getContent().remove(currentSelection.get());
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
}
