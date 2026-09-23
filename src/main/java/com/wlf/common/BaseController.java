package com.wlf.common;

import com.wlf.app.App;
import com.wlf.app.preferences.ConfigManager;
import com.wlf.common.controls.AccentedProgressBar;
import com.wlf.common.controls.OverlayStage;
import com.wlf.common.util.DirectoryUtils;
import com.wlf.common.util.FilebrowserUtils;
import com.wlf.common.util.GuiUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BaseController<T extends BaseModel> {
    protected final ObjectProperty<ConfigManager> configManager = new SimpleObjectProperty<>(ConfigManager.getInstance());
    protected ObjectProperty<T> model = new SimpleObjectProperty<>();

    @Getter
    @Setter
    private static List<Consumer<WindowEvent>> onCloseRequestCallbacks = new ArrayList<>();

    @Getter
    @Setter
    protected Stage stage;
    @Getter
    @Setter
    protected Scene scene;

    @Getter
    @Setter
    private List<OverlayStage<?>> overlays = new ArrayList<>();

    // Shared thread pool for all implementing controllers for various tasks
    protected ExecutorService executor;

    public BaseController() {
        executor = Executors.newFixedThreadPool(
                getThreadPoolSize(),
                daemonThreadFactory()
        );
    }

    @FXML
    protected abstract void initialize();

    /**
     * Called by MainController after FXMLLoader has finished initializing the GUI.
     * Currently used to execute any code that depends on values that are passed to the controller
     * after initialization, which are not available during @FXML initialize()
     */
    public void afterInit() {
        getStage().setOnCloseRequest(this::closeRequest);
    }

    public <T> void runTask(Task<T> task) {
        runTask(task, null);
    }

    public <T> void runTask(Task<T> task, ProgressAware progressAwareController) {
        if (progressAwareController != null) {
            progressAwareController.statusProperty().bind(task.messageProperty());
        }
        task.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED || newValue == Worker.State.FAILED || newValue == Worker.State.CANCELLED) {
                if (progressAwareController != null) {
                    progressAwareController.progressProperty().unbind();
                    progressAwareController.statusProperty().unbind();
                }
            }
            if (newValue == Worker.State.SUCCEEDED) {
                if (progressAwareController != null) {
                    progressAwareController.progressProperty().set(AccentedProgressBar.SUCCESSFUL_PROGRESS);
                }
            } else if (newValue == Worker.State.FAILED) {
                if (progressAwareController != null) {
                    progressAwareController.progressProperty().set(AccentedProgressBar.FAILED_PROGRESS);
                }
            } else if (newValue == Worker.State.SCHEDULED) {
                if (progressAwareController != null) {
                    progressAwareController.progressProperty().unbind();
                    progressAwareController.progressProperty().set(AccentedProgressBar.INDETERMINATE_PROGRESS);
                }
            } else if (newValue == Worker.State.RUNNING) {
                if (progressAwareController != null) {
                    progressAwareController.progressProperty().bind(task.progressProperty());
                }
            }
        });
        executor.submit(task);
    }

    /**
     * Gets the selected item of whatever table view is currently in focus.
     *
     * @param <T> type parameter for table view data model
     * @return the selected table item, null if focused control is not a table view.
     */
    protected <T> T getFocusedTableSelectionItem() {
        var table = getFocusedTable();
        if (table != null) {
            return ((TableView<T>) table).getSelectionModel().getSelectedItem();
        } else {
            return null;
        }
    }

    /**
     * Gets whatever table view is currently in focus.
     *
     * @param <T> type parameter for table view data model
     * @return the selected table, null if focused control is not a table view.
     */
    protected <T> TableView<T> getFocusedTable() {
        if (getScene().getFocusOwner() instanceof TableView<?>) {
            TableView<T> table = (TableView<T>) getScene().getFocusOwner();
            return table;
        } else {
            return null;
        }
    }

    @FXML
    protected void browseForDirectory(TextField textField) {
        DirectoryUtils.browseForDirectory((dir) -> {
            if (dir != null) {
                textField.setText(dir.getAbsolutePath());
            }
        });
    }

    @FXML
    protected void browseForFile(TextField textField) {
        FilebrowserUtils.browseForFile(System.getProperty("user.home"), (file) -> {
            if (file != null) {
                textField.setText(file.getAbsolutePath());
            }
        });
    }

    @FXML
    protected void onTextFieldDrop(DragEvent dragEvent) {
        GuiUtils.onTextFieldDrop(dragEvent);
    }

    @FXML
    protected void onDirectoryDragOver(DragEvent dragEvent) {
        DirectoryUtils.onDirectoryDragOver(dragEvent);
    }

    // Override in subclasses to set appropriate pool size
    protected int getThreadPoolSize() {
        // at least 2 because 1 FX task usually wraps multiple parallel tasks and would otherwise lock the "container task"
        return 2;
    }

    protected void closeRequest(WindowEvent windowEvent) {
        for (OverlayStage<?> overlayStage : overlays) {
            if (overlayStage.isShowing()) {
                overlayStage.close();
            }
        }
        try {
            executor.shutdown();
            ConfigManager.save();
        } catch (IOException e) {
            App.showErrorMessage(e);
        }
    }

    // Helper method to create daemon threads automatically
    protected ThreadFactory daemonThreadFactory() {
        return r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        };
    }

    public <T> void parallelForEach(Collection<T> items, Consumer<T> action, BiConsumer<Integer, Integer> onComplete) {
        int total = items.size();
        AtomicInteger processed = new AtomicInteger(0);

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (T item : items) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> action.accept(item), executor);
                futures.add(future.whenComplete((result, ex) -> {
                    if (onComplete != null) {
                        onComplete.accept(processed.incrementAndGet(), total);
                    }
                }));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------- FX Boilerplate ---------------------------------

    public ConfigManager getConfigManager() {
        return configManager.get();
    }

    public ObjectProperty<ConfigManager> configManagerProperty() {
        return configManager;
    }

    public T getModel() {
        return model.get();
    }

    public ObjectProperty<T> modelProperty() {
        return model;
    }

    public void setModel(T model) {
        this.model.set(model);
    }
}
