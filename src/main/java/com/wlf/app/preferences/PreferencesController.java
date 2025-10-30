package com.wlf.app.preferences;

import com.wlf.app.App;
import com.wlf.app.AppLoader;
import com.wlf.app.AppStyle;
import com.wlf.app.main.data.Game;
import com.wlf.common.BaseController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class PreferencesController extends BaseController<Config> {
    @FXML
    private ComboBox<Language> cmbLanguages;
    @FXML
    private ComboBox<AppStyle.Theme> cmbThemes;
    private final FontIcon warningIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
    private final BooleanProperty languageWarningVisible = new SimpleBooleanProperty(false);

    private final BooleanProperty tfeLocationValid = new SimpleBooleanProperty();
    private final BooleanProperty tseLocationValid = new SimpleBooleanProperty();

    @FXML TextField tfDirectoryDownloads;
    @FXML TextField tfDirectoryTFE;
    @FXML TextField tfDirectoryTSE;

    @FXML
    FontIcon imgCheckTFE, imgCheckTSE;

    @FXML
    Button btnBrowseDirectoryDownloads;
    @FXML Button btnBrowseDirectoryTFE;
    @FXML Button btnBrowseDirectoryTSE;
    @FXML Button btnSaveConfig;


    public PreferencesController() {
        model = Config.getInstance();
    }

    @FXML
    public void initialize() {
        warningIcon.setStyle("-fx-icon-color: red");
        cmbLanguages.setItems(FXCollections.observableList(Arrays.stream(Language.values()).toList()));
        cmbLanguages.getSelectionModel().select(model.getLanguage());
        cmbLanguages.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> {
                    App.STATE.setLanguageChanged(newValue != model.getLanguage());
                    languageWarningVisible.setValue(newValue != model.getLanguage());
                });

        cmbThemes.setItems(FXCollections.observableList(Arrays.stream(AppStyle.Theme.values()).toList()));
        cmbThemes.getSelectionModel().select(model.getActiveTheme());
        cmbThemes.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> App.setTheme(newValue));

        tfDirectoryDownloads.setText(model.getDirectoryDownloads());
        tfDirectoryTFE.setText(model.getDirectoryTFE());
        tfDirectoryTSE.setText(model.getDirectoryTSE());

        tfDirectoryTFE.textProperty().addListener((observable, oldVal, newVal) -> {
            // validate and set...
            validateTFEPath(newVal);
        });
        tfDirectoryTSE.textProperty().addListener((observable, oldVal, newVal) -> {
            validateTSEPath(newVal);
        });
        tfDirectoryDownloads.textProperty().addListener((observable, oldVal, newVal) -> {
            model.setDirectoryDownloads(newVal);
        });

        validateGamePaths();

        // register directory browser buttons
        btnBrowseDirectoryDownloads.setOnAction((event) -> {
            String path = browseForDirectory();
            model.setDirectoryDownloads(path);
        });
        btnBrowseDirectoryTFE.setOnAction((event) -> {
            String path = browseForDirectory();
            setGamePath(path, Game.TFE);
        });
        btnBrowseDirectoryTSE.setOnAction((event) -> {
            String path = browseForDirectory();
            setGamePath(path, Game.TSE);
        });
    }

    private void setGamePath(String path, Game game) {
        if (Game.TFE.equals(game)) {
            model.setDirectoryTFE(path);
        } else if (Game.TSE.equals(game)) {
            model.setDirectoryTSE(path);
        }
    }

    @FXML
    protected void onSave() throws IOException {
        validateGamePaths();
        if (App.STATE.isLanguageChanged()) {
            model.setLanguage(cmbLanguages.getValue());
            Config.save();
            AppLoader.reloadGUIs();
        } else {
            Config.save();
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
    public void validateDirectory(String path) {

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
        validateTFEPath(tfDirectoryTFE.getText());
        validateTSEPath(tfDirectoryTSE.getText());
    }

    private void validateTFEPath(String path) {
        boolean valid = Game.TFE.isGamePathValid(path);
        model.setTfeDirectoryValid(valid); // mark text if not valid
        imgCheckTFE.setVisible(valid);

        if (valid) {
            model.setDirectoryTFE(path);
        }
    }

    private void validateTSEPath(String path) {
        boolean valid = Game.TSE.isGamePathValid(path);
        model.setTseDirectoryValid(valid);
        imgCheckTSE.setVisible(valid);

        if (valid) {
            model.setDirectoryTSE(path);
        }
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

    public boolean isTfeLocationValid() {
        return tfeLocationValid.get();
    }

    public BooleanProperty tfeLocationValidProperty() {
        return tfeLocationValid;
    }

    public void setTfeLocationValid(boolean tfeLocationValid) {
        this.tfeLocationValid.set(tfeLocationValid);
    }

    public boolean isTseLocationValid() {
        return tseLocationValid.get();
    }

    public BooleanProperty tseLocationValidProperty() {
        return tseLocationValid;
    }

    public void setTseLocationValid(boolean tseLocationValid) {
        this.tseLocationValid.set(tseLocationValid);
    }
}
