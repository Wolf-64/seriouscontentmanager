package com.wlf;

import com.wlf.app.*;
import com.wlf.app.preferences.Language;
import com.wlf.common.util.Utils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class App extends javafx.application.Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class.getSimpleName());

    public static String USERNAME;
    public static Stage MAINSTAGE;

    public static String APP_TITLE = "Lightweight FX";
    public static String APP_STYLE = "";//Utils.getCss("../common/default.css");
    public static Image APP_ICON = Utils.getImageResource("app/programicon.png");
    public static FrameController FRAME_CONTROLLER;
    public static MainController MAIN_CONTROLLER;
    public final static AppState STATE = new AppState();

    public static void main(String[] args) {
        // used to display on the GUI for funsies
        USERNAME = System.getProperty("user.name").toUpperCase();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Platform.runLater(() -> showCriticalError(new Exception(throwable)));
        });
        try {
            launch();
        } catch (Exception e) {
            Platform.runLater(() -> showError(e));
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FrameController controller = appInit(stage);
        controller.afterInit();
        Application.setUserAgentStylesheet(Config.getInstance().getActiveTheme().getTheme().getUserAgentStylesheet());
        controller.loadMainGUI("app/mainView.fxml");
    }

    private FrameController appInit(Stage stage) throws IOException {
        MAINSTAGE = stage;
        AppLoader<FrameController> appLoader = new AppLoader<>("app/frame.fxml");
        Parent launcherGUI = appLoader.load();
        Scene scene = new Scene(launcherGUI);
        FrameController controller = appLoader.getController();
        FRAME_CONTROLLER = controller;
        controller.setStage(stage);
        controller.setScene(scene);

        stage.setTitle(APP_TITLE + getAppVersion());

        stage.getIcons().add(APP_ICON);
        scene.getStylesheets().add(APP_STYLE);
        stage.setScene(scene);

        stage.show();
        stage.centerOnScreen();
        return controller;
    }

    public static void setTheme(AppStyle.Theme theme) {
        Application.setUserAgentStylesheet(theme.getTheme().getUserAgentStylesheet());
        Config.getInstance().setActiveTheme(theme);
    }

    public static void showError(Exception e) {
        LOGGER.error("", e);
        ExceptionDialog dlg = new ExceptionDialog(e);
        dlg.setTitle("An exception occurred");
        dlg.setHeaderText("An uncaught exception occurred during runtime");
        dlg.initOwner(MAINSTAGE.getOwner());
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.showAndWait();
    }

    public static void showCriticalError(Exception e) {
        showError(e);
        System.exit(-1);
    }

    public static String getAppVersion() {
        return App.class.getPackage().getImplementationVersion();
    }

}