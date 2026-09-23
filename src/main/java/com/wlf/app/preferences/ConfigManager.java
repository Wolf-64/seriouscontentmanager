package com.wlf.app.preferences;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class.getName());

    /** Version to compare for potential updates and adjustments of changed or deprecated settings. */
    private static final int CURRENT_CONFIG_VERSION = 1;
    /** Path to store config file. Should be local to executed jar. */
    private static final Path CONFIG_PATH = Path.of("config.json");
    /** Mapper to read/write config.json */
    private static final ObjectMapper mapper = new ObjectMapper();

    /** Stored config version from file. Can be compared to constant to differentiate. */
    @Getter @Setter
    private int configVersion;

    private static ConfigManager _instance;

    @Getter
    private final GeneralConfig generalConfig = new GeneralConfig();

    private ConfigManager() {

    }

    private static ConfigManager load() throws IOException {
        if (Files.exists(CONFIG_PATH)) {
            String jsonString = Files.readString(CONFIG_PATH);
            _instance = mapper.readValue(jsonString, ConfigManager.class);

            if (_instance.getConfigVersion() < CURRENT_CONFIG_VERSION) {
                save();
            }
        } else {
            _instance = new ConfigManager();
            mapper.writerWithDefaultPrettyPrinter().writeValue(CONFIG_PATH.toFile(), _instance);
        }

        return _instance;
    }

    public static ConfigManager getInstance() {
        try {
            if (_instance == null) {
                _instance = load();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load configuration", e);
            _instance = new ConfigManager();
        }

        return _instance;
    }

    /**
     * Reloads configuration from disk, discarding any changes made to the object.
     */
    public static void reload() throws IOException {
        _instance = load();
    }

    /**
     * Writes global configuration file and nested configurations to disk.
     */
    public static void save() throws IOException {
        if (_instance == null) {
            _instance = new ConfigManager();
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(CONFIG_PATH.toFile(), _instance);
    }
}
