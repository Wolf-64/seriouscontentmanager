package com.wlf.app.preferences;

import com.wlf.app.App;
import com.wlf.app.AppLoader;
import com.wlf.app.AppStyle;
import com.wlf.app.logging.LogManager;
import com.wlf.app.main.data.Game;
import com.wlf.common.BaseController;
import com.wlf.common.BaseModel;
import com.wlf.common.controls.ValidatingTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeRegular;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.event.Level;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PreferencesController extends BaseController<BaseModel> {
    @FXML
    private ChoiceBox<Language> cmbLanguages;
    @FXML
    private ComboBox<AppStyle.Theme> cmbThemes;
    @FXML
    private ChoiceBox<LogManager.LogType> cmbLogType;
    @FXML
    private ChoiceBox<Level> cmbLogLevel;
    @FXML
    private ToggleButton btnDarkMode;
    @FXML
    private CheckBox cbxRestoreWindow, cbxStartFullscreen;

    private final FontIcon warningIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
    private final BooleanProperty languageWarningVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty darkModeToggleDisabled = new SimpleBooleanProperty(false);
    private final BooleanProperty logFileControlsVisible = new SimpleBooleanProperty(false);

    private final ObjectProperty<Ikon> iconDarkModeToggle = new SimpleObjectProperty<>(FontAwesomeSolid.SUN);

    private final ObjectProperty<ConfigManager> config= new SimpleObjectProperty<>(ConfigManager.getInstance());

    @FXML
    FontIcon imgCheckTFE, imgCheckTSE;

    @FXML
    private ValidatingTextField vtfTFEPath, vtfTSEPath, vtfDownloadsPath;

    @FXML
    public CheckBox cbAutoClearList;
    @FXML
    Button btnBrowseDirectoryDownloads;
    @FXML Button btnBrowseDirectoryTFE;
    @FXML Button btnBrowseDirectoryTSE;
    @FXML Button btnSaveConfig;

    @FXML
    private Spinner<Integer> spinMaxDownloads;

    @FXML
    public void initialize() {
        warningIcon.setStyle("-fx-icon-color: red");
        darkModeToggleDisabled.set(getConfig().getGeneralConfig().getActiveTheme().isSingleMode());
        btnDarkMode.selectedProperty().addListener((_, _, newValue) -> {
            if (newValue) {
                iconDarkModeToggle.set(FontAwesomeRegular.MOON);
            } else {
                iconDarkModeToggle.set(FontAwesomeSolid.SUN);
            }
        });
        btnDarkMode.selectedProperty().bindBidirectional(getConfig().getGeneralConfig().darkModeEnabledProperty());
        cbxRestoreWindow.selectedProperty().bindBidirectional(getConfig().getGeneralConfig().restoreWindowProperty());
        cbxStartFullscreen.selectedProperty().bindBidirectional(getConfig().getGeneralConfig().fullScreenProperty());
        cmbLanguages.setItems(FXCollections.observableList(Arrays.stream(Language.values()).toList()));
        cmbLanguages.getSelectionModel().select(getConfig().getGeneralConfig().getLanguage());
        cmbLanguages.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    App.STATE.setLanguageChanged(newValue != getConfig().getGeneralConfig().getLanguage());
                    languageWarningVisible.setValue(newValue != getConfig().getGeneralConfig().getLanguage());
                });
        cmbThemes.setItems(FXCollections.observableList(Arrays.stream(AppStyle.Theme.values()).toList()));
        cmbThemes.getSelectionModel().select(getConfig().getGeneralConfig().getActiveTheme());
        cmbThemes.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    setDarkModeToggleDisabled(newValue.isSingleMode());
                    if (newValue.getLightTheme() == null) {
                        getConfig().getGeneralConfig().setDarkModeEnabled(true);
                    } else if (newValue.getDarkTheme() == null) {
                        getConfig().getGeneralConfig().setDarkModeEnabled(false);
                    }

                    App.setAppTheme(newValue, getConfig().getGeneralConfig().isDarkModeEnabled());
                });
        cmbLogType.setItems(FXCollections.observableList(Arrays.stream(LogManager.LogType.values()).toList()));
        cmbLogType.getSelectionModel().select(getConfig().getGeneralConfig().getLogType());
        cmbLogType.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    getConfig().getGeneralConfig().setLogType(newValue);
                    setLogFileControlsVisible(newValue == LogManager.LogType.FILE);
                });
        setLogFileControlsVisible(getConfig().getGeneralConfig().getLogType() == LogManager.LogType.FILE);
        cmbLogLevel.setItems(FXCollections.observableList(Arrays.stream(Level.values()).toList()));
        cmbLogLevel.getSelectionModel().select(getConfig().getGeneralConfig().getLogLevel());
        cmbLogLevel.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    getConfig().getGeneralConfig().setLogLevel(newValue);
                });

        spinMaxDownloads.getValueFactory().valueProperty().bindBidirectional(getConfig().getDownloaderConfig().maxDownloadsProperty());

        vtfDownloadsPath.textProperty().bindBidirectional(getConfig().getDownloaderConfig().directoryDownloadsProperty());
        vtfDownloadsPath.setValidator(this::validateDownloadsPath);
        vtfTFEPath.textProperty().bindBidirectional(getConfig().getManagerConfig().directoryTFEProperty());
        vtfTFEPath.setValidator(this::validateTFEPath);
        vtfTSEPath.textProperty().bindBidirectional(getConfig().getManagerConfig().directoryTSEProperty());
        vtfTSEPath.setValidator(this::validateTSEPath);

        cbAutoClearList.selectedProperty().bindBidirectional(getConfig().getDownloaderConfig().autoClearFinishedDownloadsProperty());

        validateGamePaths();

        // register directory browser buttons
        btnBrowseDirectoryDownloads.setOnAction((event) -> {
            String path = browseForDirectory();
            getConfig().getDownloaderConfig().setDirectoryDownloads(path);
        });
        btnBrowseDirectoryTFE.setOnAction((event) -> {
            String path = browseForDirectory();
            getConfig().getManagerConfig().setDirectoryTFE(path);
        });
        btnBrowseDirectoryTSE.setOnAction((event) -> {
            String path = browseForDirectory();
            getConfig().getManagerConfig().setDirectoryTSE(path);
        });
    }

    @Override
    public void afterInit() {
        cmbLogType.setConverter(new StringConverter<>() {
            private final ResourceBundle bundle = getResourceBundle();

            @Override
            public String toString(LogManager.LogType logType) {
                if (logType == null) return "<empty>";
                return bundle.getString("logtype." + logType.name());
            }

            @Override
            public LogManager.LogType fromString(String s) {
                try {
                    return LogManager.LogType.valueOf(s.replace("logtype.", ""));
                } catch (IllegalArgumentException e) {
                    return LogManager.LogType.NONE;
                }
            }
        });
    }

    @FXML
    public void onToggleDarkMode() {
        App.setAppTheme(cmbThemes.getSelectionModel().getSelectedItem(), getConfig().getGeneralConfig().isDarkModeEnabled());
    }

    private boolean validateDownloadsPath(String path) {
        return Files.exists(Path.of(path));
    }

    @FXML
    protected void onSave() throws IOException {
        validateGamePaths();
        if (App.STATE.isLanguageChanged()) {
            getConfig().getGeneralConfig().setLanguage(cmbLanguages.getValue());
            ConfigManager.save();
            AppLoader.reloadGUIs();
        } else {
            ConfigManager.save();
            onCancel();
        }
    }

    @FXML
    protected void onCancel() {
        try {
            ConfigManager.reload();
        } catch (IOException e) {
            App.showErrorMessage(e);
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

    private void validateGamePaths() {
        validateTFEPath(getConfig().getManagerConfig().getDirectoryTFE());
        validateTSEPath(getConfig().getManagerConfig().getDirectoryTSE());
    }

    private boolean validateTFEPath(String path) {
        boolean valid = Game.TFE.isGamePathValid(path);
        getConfig().getManagerConfig().setTfeDirectoryValid(valid);
        imgCheckTFE.setVisible(valid);
        return valid;
    }

    private boolean validateTSEPath(String path) {
        boolean valid = Game.TSE.isGamePathValid(path);
        getConfig().getManagerConfig().setTseDirectoryValid(valid);
        imgCheckTSE.setVisible(valid);
        return valid;
    }

    // ----------------------------------- FX Boilerplate ---------------------------------------

    public boolean getLanguageWarningVisible() {
        return languageWarningVisible.get();
    }

    public BooleanProperty languageWarningVisibleProperty() {
        return languageWarningVisible;
    }

    public void setLanguageWarningVisible(boolean languageWarningVisible) {
        this.languageWarningVisible.set(languageWarningVisible);
    }

    public ConfigManager getConfig() {
        return config.get();
    }

    public ObjectProperty<ConfigManager> configProperty() {
        return config;
    }

    public void setConfig(ConfigManager config) {
        this.config.set(config);
    }

    public boolean isDarkModeToggleDisabled() {
        return darkModeToggleDisabled.get();
    }

    public BooleanProperty darkModeToggleDisabledProperty() {
        return darkModeToggleDisabled;
    }

    public void setDarkModeToggleDisabled(boolean darkModeToggleDisabled) {
        this.darkModeToggleDisabled.set(darkModeToggleDisabled);
    }

    public boolean isLogFileControlsVisible() {
        return logFileControlsVisible.get();
    }

    public BooleanProperty logFileControlsVisibleProperty() {
        return logFileControlsVisible;
    }

    public void setLogFileControlsVisible(boolean logFileControlsVisible) {
        this.logFileControlsVisible.set(logFileControlsVisible);
    }

    public Ikon getIconDarkModeToggle() {
        return iconDarkModeToggle.get();
    }

    public ObjectProperty<Ikon> iconDarkModeToggleProperty() {
        return iconDarkModeToggle;
    }

    public void setIconDarkModeToggle(Ikon iconDarkModeToggle) {
        this.iconDarkModeToggle.set(iconDarkModeToggle);
    }
}
