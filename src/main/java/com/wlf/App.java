package com.wlf;

import com.wlf.app.AppStyle;
import com.wlf.app.Config;
import com.wlf.app.FrameController;
import com.wlf.app.preferences.Language;
import com.wlf.common.util.Utils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends javafx.application.Application {

    public static String USERNAME;
    public static Stage MAINSTAGE;

    public static String APP_TITLE = "Lightweight FX";
    public static String APP_STYLE = "";//Utils.getCss("../common/default.css");
    public static Image APP_ICON = Utils.getImageResource("app/programicon.png");
    public static FrameController FRAME_CONTROLLER;

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
        controller.loadMainGUI("app/mainView.fxml");
    }

    private FrameController appInit(Stage stage) throws IOException {
        MAINSTAGE = stage;
        FXMLLoader loader = new FXMLLoader(App.class.getResource("app/frame.fxml"));
        Parent launcherGUI = loader.load();
        Scene scene = new Scene(launcherGUI);
        FrameController controller = loader.getController();
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

    public static void setLanguage(Language language) {
        Config.getInstance().setLanguage(language);
    }

    public static void setTheme(AppStyle.Theme theme) {
        Application.setUserAgentStylesheet(theme.getTheme().getUserAgentStylesheet());
        Config.getInstance().setActiveTheme(theme);
    }

    public static void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.WARNING,
                "An error occurred:"
                        + System.lineSeparator()
                        + System.lineSeparator()
                        + e);
        alert.showAndWait();
    }

    public static void showCriticalError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "A critical error occurred. The application will now shutdown."
                        + System.lineSeparator()
                        + System.lineSeparator()
                        + e);
        alert.showAndWait();
        System.exit(-1);
    }

    public static String getAppVersion() {
        return App.class.getPackage().getImplementationVersion();
    }

}