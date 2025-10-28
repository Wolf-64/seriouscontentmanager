package com.wlf.app;

import com.wlf.App;
import com.wlf.common.*;
import com.wlf.common.util.AsyncFXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import org.controlsfx.control.MaskerPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;

public final class FrameController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameController.class.getSimpleName());
    private static final boolean DEV_MODE = Config.getInstance().isDevMode();

    @FXML
    private VBox splash;
    @FXML
    private BorderPane content, preferences;
    @FXML
    private MaskerPane loadingMask;

    private String mainGuiFXML;

    public FrameController(){}


    @FXML
    @Override
    protected void initialize() {

    }

    public void loadMainGUI(String fxml) throws IOException {
        splash.setVisible(true);
        AsyncFXMLLoader loader = new AsyncFXMLLoader(fxml);

        loader.setOnSucceeded((fxmlLoader) -> {
            content.setCenter(fxmlLoader.getGui());
            App.MAIN_CONTROLLER = (MainController) fxmlLoader.getController();
            splash.setVisible(false);
            mainGuiFXML = fxml;
        });
        loader.load();
    }

    public void reloadGUI() throws IOException {
        content.setCenter(null);
        AsyncFXMLLoader loader = new AsyncFXMLLoader(mainGuiFXML);
        loader.setOnSucceeded((gui) -> {
            content.setCenter(gui.getGui());
        });
        loader.load();
    }

    @Override
    public void afterInit() {
    }

    private void checkInternetConnection() {
        InetAddress address = null;
        try {
            address = InetAddress.getByName("google.com");

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