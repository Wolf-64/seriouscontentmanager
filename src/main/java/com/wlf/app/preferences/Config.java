package com.wlf.app.preferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wlf.app.AppStyle;
import com.wlf.common.BaseModel;
import javafx.beans.property.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Global configuration object containing general settings and tool-specific configurations.
 * Is loaded from and stored to JSON for simplicity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config extends BaseModel {
    /** Version to compare for potential updates and adjustments of changed or deprecated settings. */
    private static final int CURRENT_CONFIG_VERSION = 1;
    /** Path to store config file. Should be local to executed jar. */
    private static final String CONFIG_PATH = "config.json";
    /** Mapper to read/write config.json */
    private static final ObjectMapper mapper = new ObjectMapper();

    /** Active theme index from the global list of themes.*/
    private final ObjectProperty<AppStyle.Theme> activeTheme = new SimpleObjectProperty<>(AppStyle.Theme.PRIMER_LIGHT);

    /** Stored config version from file. Can be compared to constant to differentiate. */
    @Getter @Setter
    private int configVersion;

    private final ObjectProperty<Language> language = new SimpleObjectProperty<>(Language.ENGLISH);

    // not used in UI and only for dev
    @Getter @Setter
    private boolean devMode;

    private static Config _config;

    /** Attempts to load an already stored configuration from disk. If non is found, it will create a new object. */
    public static Config getInstance() {
        try {
            if (_config == null) {
                _config = load();
            }

            boolean newVersion = _config.getConfigVersion() < CURRENT_CONFIG_VERSION;

            // write new properties immediately on new versions
            if (newVersion) {
                _config.setConfigVersion(CURRENT_CONFIG_VERSION);
                save();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return _config;
    }

    /** Reloads configuration from disk, discarding any changes made to the object. */
    public static void reload() throws IOException {
        _config = load();
    }

    /** Creates new Config object from loading the json from disk. Returns a new object if no config file exists. */
    private static Config load() throws IOException {
        Config conf = null;
        Path confPath = Path.of(CONFIG_PATH);
        if (Files.exists(confPath)) {
            String jsonString = Files.readString(confPath);
            conf = mapper.readValue(jsonString, Config.class);
        }

        if (conf == null) {
            conf = new Config();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_PATH), conf);
        }

        return conf;
    }

    /** Writes global configuration file and nested configurations to disk. */
    public static void save() throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_PATH), _config);
    }

    // --- JavaFX boilerplate that's not covered by Lombok ---


    public AppStyle.Theme getActiveTheme() {
        return activeTheme.get();
    }

    public ObjectProperty<AppStyle.Theme> activeThemeProperty() {
        return activeTheme;
    }

    public void setActiveTheme(AppStyle.Theme activeTheme) {
        this.activeTheme.set(activeTheme);
    }

    public Language getLanguage() {
        return language.get();
    }

    public ObjectProperty<Language> languageProperty() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language.set(language);
    }
}

