package com.wlf.app;

import com.wlf.App;
import com.wlf.common.*;
import com.wlf.common.util.ErrorHandler;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;

public final class FrameController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameController.class.getSimpleName());
    private static final boolean DEV_MODE = Config.getInstance().isDevMode();

    @FXML
    private VBox splash;
    @FXML
    private BorderPane content, preferences;
    @FXML
    @Override
    protected void initialize() {

    }

    public void loadGUI(String fxml) throws IOException {
        splash.setVisible(true);
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
                // TODO remove when a substantial loading times have been reached
                Thread.sleep(1000);
                return loader.load();
            }
        };

        loadTask.setOnFailed(ErrorHandler::defaultWorkerStateEventError);
        loadTask.setOnSucceeded((wse) -> {
            try {
                Parent gui = loadTask.get();
                content.setCenter(gui);
                splash.setVisible(false);
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        });

        new Thread(loadTask).start();
    }

    @Override
    public void afterInit() {

    }

    @FXML
    private void showPreferences() {
        if (preferences.getCenter() != null) {
            Task<Parent> loadTask = new Task<>() {
                @Override
                protected Parent call() throws Exception {
                    FXMLLoader loader = new FXMLLoader(App.class.getResource("app/preferences.fxml"));
                    return loader.load();
                }
            };

            loadTask.setOnFailed(ErrorHandler::defaultWorkerStateEventError);
            loadTask.setOnSucceeded((wse) -> {
                try {
                    Parent preferencesPane = loadTask.get();
                    content.setVisible(false);
                    preferences.setCenter(preferencesPane);
                    preferences.setVisible(true);
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                    throw new RuntimeException(e);
                }
            });

            new Thread(loadTask).start();
        } else {
            content.setVisible(false);
            preferences.setVisible(true);
        }
    }

    @FXML
    public void hidePreferences() {
        content.setVisible(true);
        preferences.setVisible(false);
    }

    private void checkInternetConnection() {
        InetAddress address = null;
        try {
            address = InetAddress.getByName("www.confluence.dedalus.com");

            //setEnableWifiIcon(!address.isReachable(5000));
        } catch (IOException e) {
            //setEnableWifiIcon(true);
        }
    }

    /**
     * Makes a call to the GitHub API and therefore requires an API token to be present in order to work.
     * Will check for the current release version string and compare to the running app to show an
     * indicator in case the current running version is lower.
     * <p>
     * In case GitHub cannot be reached, or no API key is present, a different icon will represent that.
     */
    public void checkForUpdates() {
/*
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("Authorization", "Bearer " + credentialsToUse.getPassword())
                    .timeout(Duration.ofSeconds(20))
                    .uri(URI.create(url)).GET().build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            (GITHUB_API_PREFIX + projectUrl,
                    credentialsProvider.getCredentials(RemoteType.GITHUB, this::validateCredentials));

            JsonNode node = new ObjectMapper().readTree(response.body());

            node.get("html_url").asText(); // tag release url
        } catch (IOException | InterruptedException e) {

        }

 */
    }

    @Override
    protected void closeRequest(WindowEvent windowEvent) {

    }
}