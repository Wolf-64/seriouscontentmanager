package com.wlf.app.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wlf.app.AppLoader;
import com.wlf.app.main.net.GroRepositoryController;
import com.wlf.app.preferences.Config;
import com.wlf.app.preferences.PreferencesController;
import com.wlf.common.BaseController;
import com.wlf.app.main.data.*;
import com.wlf.app.main.io.FileHandler;
import com.wlf.app.main.io.GameHandler;

import com.wlf.common.util.LocalDateTimeStringConverter;
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
    private final ObjectProperty<Filter> tableFilter = new SimpleObjectProperty<>(new Filter());

    @FXML
    TabPane tabPane;
    @FXML
    Tab settings, grorepo;

    // --- Filter ---
    @FXML
    TextField tfNameFilter;
    @FXML
    RadioButton rbTfe, rbTse, rbMaps, rbMods, rbSp, rbCoop, rbDm;
    @FXML
    CheckBox cbInstalled, cbCompleted;

    @FXML
    ToggleGroup tgGame, tgType, tgMode;
    @FXML
    TableView<ContentModel> table;
    @FXML
    TableColumn<ContentModel, Void> actionColumn;
    @FXML
    TableColumn<ContentModel, LocalDateTime> colDateAdded;
    @FXML
    TableColumn<ContentModel, String> colDateCreated;

    // --- TableView context menu ---
    @FXML
    MenuItem menuItemInstall;
    @FXML
    MenuItem menuItemRemove;

    private final DBManager dbManager = DBManager.getInstance();
    private GroRepositoryController repoController;

    private final BooleanProperty installDisabled = new SimpleBooleanProperty();
    private final BooleanProperty installVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty installMultiDisabled = new SimpleBooleanProperty();
    private final BooleanProperty installMultiVisible = new SimpleBooleanProperty();
    private final BooleanProperty removeDisabled = new SimpleBooleanProperty();
    private final BooleanProperty removeVisible = new SimpleBooleanProperty(true);

    private final ObjectProperty<ContentModel> currentSelection = new SimpleObjectProperty<>();

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

        for (ContentEntity entity : ContentRepository.getInstance().findAll()) {
            ContentModel contentModel = ContentMapper.INSTANCE.toGui(entity);
            contentModel.completedProperty().addListener(getListItemListener(contentModel));
            getModel().getContent().add(contentModel);
        }

        colDateAdded.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateTimeStringConverter()));
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

    private void initBindings() {
        getTableFilter().setGameSelected(((Game) tgGame.getSelectedToggle().getUserData()).ordinal());
        getTableFilter().setTypeSelected(((Type) tgType.getSelectedToggle().getUserData()).ordinal());
        getTableFilter().setModeSelected(((Mode) tgMode.getSelectedToggle().getUserData()).ordinal());
        cbInstalled.selectedProperty().bindBidirectional(getTableFilter().installedProperty());
        cbCompleted.selectedProperty().bindBidirectional(getTableFilter().completedProperty());
        tfNameFilter.textProperty().bindBidirectional(getTableFilter().nameProperty());

        tgGame.selectedToggleProperty().addListener((property, oldVal, newVal) -> {
            getTableFilter().setGameSelected(((Game) newVal.getUserData()).ordinal());
        });
        tgMode.selectedToggleProperty().addListener((property, oldVal, newVal) -> {
            getTableFilter().setModeSelected(((Mode) newVal.getUserData()).ordinal());
        });
        tgType.selectedToggleProperty().addListener((property, oldVal, newVal) -> {
            getTableFilter().setTypeSelected(((Type) newVal.getUserData()).ordinal());
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

    ChangeListener<Object> filterListener = ((observable, oldVal, newVal) -> {
        applyFilter();
    });

    private void applyFilter() {
        getModel().getContent().setAll(dbManager.getFileEntries(getTableFilter()));
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

    public Filter getTableFilter() {
        return tableFilter.get();
    }

    public ObjectProperty<Filter> tableFilterProperty() {
        return tableFilter;
    }

    public void setTableFilter(Filter tableFilter) {
        this.tableFilter.set(tableFilter);
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
