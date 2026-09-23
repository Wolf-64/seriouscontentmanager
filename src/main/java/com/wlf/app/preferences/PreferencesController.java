package com.wlf.app.preferences;

import com.wlf.app.App;
import com.wlf.app.AppLoader;
import com.wlf.app.AppStyle;
import com.wlf.app.main.data.Game;
import com.wlf.common.BaseController;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome6.FontAwesomeRegular;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class PreferencesController extends BaseController<GeneralConfig> {
    @FXML
    private ComboBox<Language> cmbLanguages;
    @FXML
    private ComboBox<AppStyle.Theme> cmbThemes;
    @FXML
    private ToggleButton btnDarkMode;
    @FXML
    private CheckBox cbxRestoreWindow, cbxStartFullscreen;

    private final FontIcon warningIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
    private final BooleanProperty languageWarningVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty darkModeToggleDisabled = new SimpleBooleanProperty(false);

    private final ObjectProperty<Ikon> iconDarkModeToggle = new SimpleObjectProperty<>(FontAwesomeSolid.SUN);

    private final ObjectProperty<GeneralConfig> config= new SimpleObjectProperty<>(ConfigManager.getInstance().getGeneralConfig());

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


    public PreferencesController() {
        model.set(ConfigManager.getInstance().getGeneralConfig());
    }

    @FXML
    public void initialize() {
        warningIcon.setStyle("-fx-icon-color: red");
        btnDarkMode.selectedProperty().bindBidirectional(getConfig().darkModeEnabledProperty());
        cbxRestoreWindow.selectedProperty().bindBidirectional(getConfig().restoreWindowProperty());
        cbxStartFullscreen.selectedProperty().bindBidirectional(getConfig().fullScreenProperty());
        cmbLanguages.setItems(FXCollections.observableList(Arrays.stream(Language.values()).toList()));
        cmbLanguages.getSelectionModel().select(model.get().getLanguage());
        cmbLanguages.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    App.STATE.setLanguageChanged(newValue != model.get().getLanguage());
                    languageWarningVisible.setValue(newValue != model.get().getLanguage());
                });

        cmbThemes.setItems(FXCollections.observableList(Arrays.stream(AppStyle.Theme.values()).toList()));
        cmbThemes.getSelectionModel().select(model.get().getActiveTheme());
        cmbThemes.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    if (newValue.getLightTheme() == null) {
                        getConfig().setDarkModeEnabled(true);
                        setDarkModeToggleDisabled(true);
                        onToggleDarkMode();
                    } else if (newValue.getDarkTheme() == null) {
                        getConfig().setDarkModeEnabled(false);
                        setDarkModeToggleDisabled(true);
                        onToggleDarkMode();
                    } else {
                        App.setAppTheme(newValue, getConfig().isDarkModeEnabled());
                    }
                });

        spinMaxDownloads.getValueFactory().valueProperty().bindBidirectional(getModel().maxDownloadsProperty());

        vtfDownloadsPath.textProperty().bindBidirectional(getModel().directoryDownloadsProperty());
        vtfDownloadsPath.setValidator(this::validateDownloadsPath);
        vtfTFEPath.textProperty().bindBidirectional(getModel().directoryTFEProperty());
        vtfTFEPath.setValidator(this::validateTFEPath);
        vtfTSEPath.textProperty().bindBidirectional(getModel().directoryTSEProperty());
        vtfTSEPath.setValidator(this::validateTSEPath);

        cbAutoClearList.selectedProperty().bindBidirectional(getModel().autoClearFinishedDownloadsProperty());

        validateGamePaths();

        // register directory browser buttons
        btnBrowseDirectoryDownloads.setOnAction((event) -> {
            String path = browseForDirectory();
            getModel().setDirectoryDownloads(path);
        });
        btnBrowseDirectoryTFE.setOnAction((event) -> {
            String path = browseForDirectory();
            getModel().setDirectoryTFE(path);
        });
        btnBrowseDirectoryTSE.setOnAction((event) -> {
            String path = browseForDirectory();
            getModel().setDirectoryTSE(path);
        });
    }

    @FXML
    public void onToggleDarkMode() {
        if (getConfig().isDarkModeEnabled()) {
            iconDarkModeToggle.set(FontAwesomeRegular.MOON);
        } else {
            iconDarkModeToggle.set(FontAwesomeSolid.SUN);
        }
        App.setAppTheme(cmbThemes.getSelectionModel().getSelectedItem(), getConfig().isDarkModeEnabled());
                (_, _, newValue) -> App.setTheme(newValue));
    }

    private boolean validateDownloadsPath(String path) {
        return Files.exists(Path.of(path));
    }

    @FXML
    protected void onSave() throws IOException {
        validateGamePaths();
        if (App.STATE.isLanguageChanged()) {
            model.get().setLanguage(cmbLanguages.getValue());
            ConfigManager.save();
            AppLoader.reloadGUIs();
        } else {
            ConfigManager.save();
            onCancel();
        }
    }

    @FXML
    protected void onCancel() {

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
        validateTFEPath(getModel().getDirectoryTFE());
        validateTSEPath(getModel().getDirectoryTSE());
    }

    private boolean validateTFEPath(String path) {
        boolean valid = Game.TFE.isGamePathValid(path);
        getModel().setTfeDirectoryValid(valid);
        imgCheckTFE.setVisible(valid);
        return valid;
    }

    private boolean validateTSEPath(String path) {
        boolean valid = Game.TSE.isGamePathValid(path);
        getModel().setTseDirectoryValid(valid);
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

    public GeneralConfig getConfig() {
        return config.get();
    }

    public ObjectProperty<GeneralConfig> configProperty() {
        return config;
    }

    public void setConfig(GeneralConfig config) {
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
}
