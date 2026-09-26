package com.wlf.app;

import atlantafx.base.theme.ThemeManager;
import atlantafx.base.theme.ThemeOption;
import com.dlsc.gemsfx.util.ControlsFXAtlantaFX;
import com.dlsc.gemsfx.util.GemsFXAtlantaFX;
import com.wlf.app.preferences.ConfigManager;
import com.wlf.common.BaseController;
import com.wlf.common.themes.BaseTheme;
import com.wlf.common.util.Utils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
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

    private static Stage ABOUT_STAGE;

    private static final ThemeOption.Key<Boolean> THEME_INTEGRATIONS =
            new ThemeOption.Key<>("app.theme.integrations", Boolean.class);

    public static void main(String[] args) {
        // used to display on the GUI for funsies
        USERNAME = System.getProperty("user.name").toUpperCase();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Platform.runLater(() -> showCriticalError(new Exception(throwable)));
        });
        try {
            launch();
        } catch (Exception e) {
            Platform.runLater(() -> showErrorMessage(e));
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FrameController controller = appInit(stage);
        controller.afterInit();
        ThemeManager.instance().register(ThemeOption.of(
                THEME_INTEGRATIONS,
                Boolean.TRUE,
                change -> applyThemeIntegration(change.scene(), (BaseTheme) (change.theme()))
        ));
        setAppTheme(ConfigManager.getInstance().getGeneralConfig().getActiveTheme(),
                ConfigManager.getInstance().getGeneralConfig().isDarkModeEnabled());
        ThemeManager.instance().setOption(THEME_INTEGRATIONS, Boolean.TRUE);
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
        controller.setWindowsSizeFromConfig();

        stage.setTitle(getAppName() + " v" + getAppVersion());

        stage.getIcons().add(APP_ICON);
        stage.setScene(scene);

        // save and restore window size
        MAINSTAGE.widthProperty().addListener((_, _, newValue) -> ConfigManager.getInstance().getGeneralConfig().setWindowWidth(newValue.doubleValue()));
        MAINSTAGE.heightProperty().addListener((_, _, newValue) -> ConfigManager.getInstance().getGeneralConfig().setWindowHeight(newValue.doubleValue()));

        MAINSTAGE.fullScreenProperty().addListener((_, _, newValue) -> ConfigManager.getInstance().getGeneralConfig().setFullScreen(newValue));

        stage.show();
        return controller;
    }

    public static void setAppTheme(AppStyle.Theme theme, boolean darkMode) {
        MAINSCENE.getStylesheets().clear();
        // Null reset forces JavaFX to flush CSS cache
        Application.setUserAgentStylesheet(null);

        ThemeManager.instance().setTheme(darkMode ? theme.getDarkTheme() : theme.getLightTheme());

        ConfigManager.getInstance().getGeneralConfig().setActiveTheme(theme);
        ConfigManager.getInstance().getGeneralConfig().setDarkModeEnabled(darkMode);
    }

    /**
     * Applies the GemsFX/ControlsFX AtlantaFX integration stylesheets to the given scene when the
     * currently active theme is AtlantaFX-based. Must be called for every scene that hosts GemsFX or
     * ControlsFX controls (main window, login popup, overlay panes) - otherwise those controls' icons
     * fall back to non-theme-aware colors baked into the library's default stylesheets.
     */
    private static void applyThemeIntegration(Scene scene, BaseTheme activeTheme) {
        scene.getStylesheets().clear();
        if (activeTheme.getSceneStyleSheet() != null) {
            scene.getStylesheets().add(activeTheme.getSceneStyleSheet());
        }
        if (scene == null || activeTheme == null || !activeTheme.isAtlantaFX()) {
            return;
        }

        ControlsFXAtlantaFX.applyTo(scene);
        GemsFXAtlantaFX.applyTo(scene);
    }

    public static void showErrorMessage(Throwable exception) {
        ExceptionDialog dialog = new ExceptionDialog(exception);
        dialog.setTitle("Exception occurred");
        dialog.setHeaderText("An uncaught exception occurred " + exception.getCause());
        dialog.initOwner(MAINSTAGE);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }

    public static Alert createAlert(Alert.AlertType alertType) {
        return createAlert(alertType, "");
    }

    public static Alert createAlert(Alert.AlertType alertType, String contentText) {
        return createAlert(alertType, contentText, ButtonType.OK);
    }

    public static Alert createAlert(Alert.AlertType alertType, String contentText, ButtonType... buttons) {
        Alert alert = new Alert(alertType, contentText, buttons);
        alert.initModality(Modality.APPLICATION_MODAL);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(APP_ICON);
        Window ownerWindow = getFocusedWindow();
        if (ownerWindow != null) {
            alert.initOwner(ownerWindow);
            centerDialogInStage(alert, ownerWindow);
            // controlsFX doesn't apply atlanta styles everywhere, so we manually need to override some
            MAINSCENE.getStylesheets().add(App.class.getResource("/com/wlf/common/themes/controlsfx-override.css").toExternalForm());
        }

        return alert;
    }

    public static Window getFocusedWindow() {
        return Window.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(MAINSTAGE);
    }

    public static void centerDialogInStage(Dialog<?> dialog, Window ownerWindow) {
        dialog.setOnShown(event -> {
            double centerXPosition = ownerWindow.getX() + ownerWindow.getWidth() / 2d;
            double centerYPosition = ownerWindow.getY() + ownerWindow.getHeight() / 2d;
            Window dialogWindow = dialog.getDialogPane().getScene().getWindow();
            dialogWindow.setX(centerXPosition - dialogWindow.getWidth() / 2d);
            dialogWindow.setY(centerYPosition - dialogWindow.getHeight() / 2d);
        });
    }

    public static void showAboutDialog() {
        if (ABOUT_STAGE == null) {
            try {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/wlf/app/about.fxml"));
                Parent window = loader.load();
                Scene scene = new Scene(window);
                Stage stage = new Stage();
                ABOUT_STAGE = stage;

                stage.setTitle(getAppName() + " v" + getAppVersion());

                stage.getIcons().add(APP_ICON);
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);

                stage.show();
            } catch (IOException exception) {
                showErrorMessage(exception);
            }
        } else {
            ABOUT_STAGE.show();
        }
    }

    public static void showCriticalError(Exception e) {
        showErrorMessage(e);
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