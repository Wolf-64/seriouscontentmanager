package com.wlf.common.util;

import com.wlf.App;
import com.wlf.common.BaseController;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.Getter;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class AsyncFXMLLoader {
    private final Task<AsyncFXMLLoader> loaderTask;
    @Getter
    private BaseController controller;
    @Getter
    private Parent gui;

    public AsyncFXMLLoader(String fxml) {
        loaderTask = new Task<>() {
            @Override
            protected AsyncFXMLLoader call() throws Exception {
                FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
                controller = loader.getController();
                loader.setResources(App.I18N);
                Parent parent = loader.load();
                controller = loader.getController();
                return getThis();
            }
        };

        loaderTask.setOnFailed(ErrorHandler::defaultWorkerStateEventError);
    }

    private AsyncFXMLLoader getThis() {
        return this;
    }

    public void load() {
        new Thread(loaderTask).start();
    }

    public void setOnSucceeded(Consumer<AsyncFXMLLoader> onSucceeded) {
        loaderTask.setOnSucceeded(workerStateEvent -> {
            try {
                onSucceeded.accept(loaderTask.get());
            } catch (InterruptedException | ExecutionException e) {
                ErrorHandler.defaultWorkerStateEventError(workerStateEvent);
            }
        });
    }
}
