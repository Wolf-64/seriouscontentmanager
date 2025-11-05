package com.wlf.app;

import com.wlf.app.preferences.Config;
import com.wlf.common.BaseController;
import com.wlf.common.util.ErrorHandler;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.Parent;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class AppLoader<T extends BaseController<?>> {
    private final static Map<String, Consumer<Parent>> loadedGuis = new HashMap<>();

    private final Task<Void> loaderTask;
    @Getter
    private T controller;
    private Parent gui;
    private final String fxml;
    private final String guiName;
    private final Consumer<Parent> target;
    private Consumer<T> onSucceededConsumer;

    public AppLoader(String fxml, Consumer<Parent> target) {
        this.fxml = fxml;
        this.guiName = Path.of(fxml).getFileName().toString().replace(".fxml", "");
        this.target = target;
        loaderTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (!App.STATE.isInitializing()) {
                    App.FRAME_CONTROLLER.setLoading(true);
                }
                FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
                loader.setResources(getI18NResourceForLocale(guiName, Config.getInstance().getLanguage().getLocale()));
                gui = loader.load();
                controller = loader.getController();
                return null;
            }
        };

        loaderTask.setOnFailed(ErrorHandler::defaultWorkerStateEventError);
        loaderTask.setOnSucceeded((_ -> onSucceeded()));

    }

    public AppLoader(String fxml) {
        this.fxml = fxml;
        this.guiName = Path.of(fxml).getFileName().toString().replace(".fxml", "");
        this.target = null;
        this.loaderTask = null;
    }

    public void setOnSucceeded(Consumer<T> action) {
        this.onSucceededConsumer = action;
    }

    public void loadAsync() {
        new Thread(loaderTask).start();
    }

    /** Does not register an fxml for reloading */
    public Parent load() throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
        loader.setResources(getI18NResourceForLocale(guiName, Config.getInstance().getLanguage().getLocale()));
        gui = loader.load();
        controller = loader.getController();

        return gui;
    }

    public static void reloadGUIs() throws LoadException {
        for (String fxml : loadedGuis.keySet()) {
            reloadGui(fxml);
        }
        App.STATE.setLanguageChanged(false);
    }

    @SuppressWarnings("unchecked, rawtypes")
    public static void reloadGui(String fxml) throws LoadException {
        if (loadedGuis.containsKey(fxml)) {
            AppLoader appLoader = new AppLoader(fxml, loadedGuis.get(fxml));
            appLoader.loadAsync();
        } else {
            throw new LoadException("No such GUI loaded before: " + fxml);
        }
    }

    private void onSucceeded() {
        if (target != null) {
            target.accept(gui);
        }
        if (!loadedGuis.containsKey(fxml)) {
            loadedGuis.put(this.fxml, this.target);
        }
        if (onSucceededConsumer != null) {
            onSucceededConsumer.accept(controller);
        }
        controller.setStage(App.MAINSTAGE);
        controller.setScene(App.MAINSCENE);
        controller.afterInit();
        App.FRAME_CONTROLLER.setLoading(false);
    }

    private ResourceBundle getI18NResourceForLocale(String view, Locale locale) {
        return ResourceBundle.getBundle("com.wlf.app.i18n." + view, locale);
    }
}
