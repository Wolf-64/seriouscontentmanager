package com.wlf.common.util;

import com.wlf.App;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class AsyncFXMLLoader {
    private final Task<Parent> loaderTask;

    public AsyncFXMLLoader(String fxml) {
        loaderTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
                return loader.load();
            }
        };

        loaderTask.setOnFailed(ErrorHandler::defaultWorkerStateEventError);
    }

    public void load() {
        new Thread(loaderTask).start();
    }

    public void setOnSucceeded(Consumer<Parent> onSucceeded) {
        loaderTask.setOnSucceeded(workerStateEvent -> {
            try {
                onSucceeded.accept(loaderTask.get());
            } catch (InterruptedException | ExecutionException e) {
                ErrorHandler.defaultWorkerStateEventError(workerStateEvent);
            }
        });
    }
}
