package com.wlf.app;

import com.wlf.app.preferences.Config;
import com.wlf.common.BaseController;
import com.wlf.common.util.Utils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class App extends javafx.application.Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class.getSimpleName());

    public static String USERNAME;
    public static Stage MAINSTAGE;
    public static Scene MAINSCENE;

    public static Image APP_ICON = Utils.getImageResource("programicon.png");
    public static FrameController FRAME_CONTROLLER;
    public static BaseController<?> MAIN_CONTROLLER;
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
        setTheme(Config.getInstance().getActiveTheme());
        controller.loadMainGUI("main/mainView.fxml");
    }

    private FrameController appInit(Stage stage) throws IOException {
        MAINSTAGE = stage;
        AppLoader<FrameController> appLoader = new AppLoader<>("frame.fxml");
        Parent launcherGUI = appLoader.load();
        Scene scene = new Scene(launcherGUI);
        MAINSCENE = scene;
        FrameController controller = appLoader.getController();
        FRAME_CONTROLLER = controller;
        controller.setStage(stage);
        controller.setScene(scene);

        stage.setTitle(getAppName() + " v" + getAppVersion());

        stage.getIcons().add(APP_ICON);
        stage.setScene(scene);

        stage.show();
        return controller;
    }

    public static void setTheme(AppStyle.Theme theme) {
        // Modena Dark sits on top of Modena, so it needs a bit of a special treatment
        if (theme == AppStyle.Theme.MODENA_DARK) {
            Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
            MAINSCENE.getStylesheets().add(theme.getTheme().getUserAgentStylesheet());
        } else {
            MAINSCENE.getStylesheets().clear();
            Application.setUserAgentStylesheet(theme.getTheme().getUserAgentStylesheet());
            // controlsFX doesn't apply atlanta styles everywhere, so we manually need to override some
            MAINSCENE.getStylesheets().add(App.class.getResource("/com/wlf/common/themes/controlsfx-override.css").toExternalForm());
        }
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
        try (var is = App.class.getResourceAsStream("/meta.properties")) {
            var props = new java.util.Properties();
            props.load(is);
            return props.getProperty("app.version");
        } catch (Exception e) {
            return "dev";
        }
    }

    public static String getAppName() {
        try (var is = App.class.getResourceAsStream("/meta.properties")) {
            var props = new java.util.Properties();
            props.load(is);
            return props.getProperty("app.name");
        } catch (Exception e) {
            return "unknown app";
        }
    }
}