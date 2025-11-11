package com.wlf.app.main.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.wlf.app.main.net.ModInfo;
import com.wlf.app.main.net.Requester;
import com.wlf.app.preferences.Config;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

public class ContentModel {
    public static final Logger log = Logger.getLogger(ContentModel.class.getSimpleName());

    private static final Config config = Config.getInstance();

    /** Name of the map/mod/character model */
    private final StringProperty name = new SimpleStringProperty();
    /** Filename of either the .gro or zip-file that was downloaded */
    private final StringProperty downloadedFileName = new SimpleStringProperty();
    /** Game this content works with/was made for */
    private final ObjectProperty<Game> game = new SimpleObjectProperty<>();
    /** Type of content (map/mod/character) */
    private final ObjectProperty<Type> type = new SimpleObjectProperty<>();
    /** Game modes this content works with (SP/Co-op/DM) */
    private final ObjectProperty<Mode> modes = new SimpleObjectProperty<>();
    /** Whether this content is currently installed. */
    private final BooleanProperty installed = new SimpleBooleanProperty();
    /** Whether the content was manually marked as completed */
    private final BooleanProperty completed = new SimpleBooleanProperty();
    /** The date this content has been added/downloaded to the library */
    private final ObjectProperty<LocalDateTime> dateAdded = new SimpleObjectProperty<>();
    /** The original creation date of this content */
    private final ObjectProperty<LocalDate> dateCreated = new SimpleObjectProperty<>();
    /** The original creation date of this content */
    private final ObjectProperty<LocalDateTime> dateCompleted = new SimpleObjectProperty<>();
    /** The original creation date of this content */
    private final ObjectProperty<LocalDateTime> dateLastPlayed = new SimpleObjectProperty<>();
    /** The rating that has been given to the content */
    private final DoubleProperty rating = new SimpleDoubleProperty();
    /** Absolute path of the content's file (gro or zip) after download */
    private final ObjectProperty<ContentFile> downloadedFile = new SimpleObjectProperty<>();
    /** Absolute path of the content's file (gro) after installing */
    private final ObjectProperty<File> installFileLocation = new SimpleObjectProperty<>();
    /** All files that have been extracted from zipped content */
    private final ObservableList<File> installedFiles = FXCollections.observableArrayList();
    /** Either the URL this content has been downloaded from, or 'local' if it has been added manually */
    private final StringProperty origin = new SimpleStringProperty();
    /** The content's version */
    private final StringProperty version = new SimpleStringProperty();
    /** Total file size of the content (gro or zip) */
    private final LongProperty size = new SimpleLongProperty();

    /** GRO Repository file/mod id */
    @Getter
    private String repoId;
    /** Database ID */
    @Getter @Setter
    private Long id;

    public ContentModel() {    }

    public boolean canInstallToTfe() {
        return !isInstalled() && config.isTfeDirectoryValid();
    }

    public boolean canInstallToTse() {
        return !isInstalled() && config.isTseDirectoryValid();
    }

    public boolean canRemoveFromTfe() {
        return isInstalled() && config.isTfeDirectoryValid();
    }

    public boolean canRemoveFromTse() {
        return isInstalled() && config.isTseDirectoryValid();
    }

    /**
     * Checks for TFE or TSE separately, based on set Game. Ignores ANY.
     * @return
     */
    public boolean canInstall() {
        return switch (game.get()) {
            case TFE -> config.isTfeDirectoryValid() && !isInstalled();
            case TSE -> config.isTseDirectoryValid() && !isInstalled();
            default -> false;
        };
    }

    /**
     * Checks for TFE or TSE separately, based on set Game. Ignores ANY.
     * @return
     */
    public boolean canRemove() {
        return switch (game.get()) {
            case TFE -> config.isTfeDirectoryValid() && isInstalled();
            case TSE -> config.isTseDirectoryValid() && isInstalled();
            default -> false;
        };
    }

    /**
     *
     * @param json response from grorepository
     * @return
     */
    public ContentModel fromJSON(JsonNode json) {
        origin.set(Requester.MOD_SITE_URL + json.get("transliteratedTitle").asText());
        repoId = json.get("id").asText();
        name.set(json.get("title").asText());
        type.set(Type.values()[json.get("type").asInt()]); // 0 map 2 skin 1 mod
        game.set(Game.values()[json.get("game").asInt()]); // 0 TFE? 1 TSE 2 both?
        modes.set(Mode.values()[json.get("subcategory").asInt()]); // mode? 4 all, 2 = sp + coop
        size.set(json.get("size").asLong());
        try {
            version.set(json.get("version").asText());
            dateCreated.set(LocalDate.parse(json.get("originalCreatedDate").asText()));
        } catch (DateTimeParseException ex) {
            log.warning(ex.getMessage());
        }

        return this;
    }

    public ContentModel fromModInfo(ModInfo modInfo) {
        origin.set(Requester.MOD_SITE_URL + modInfo.getTransliteratedTitle());
        repoId = "" + modInfo.getId();
        name.set(modInfo.getTitle());
        type.set(modInfo.getType()); // 0 map 2 skin 1 mod
        game.set(modInfo.getGame()); // 0 TFE? 1 TSE 2 both?
        modes.set(modInfo.getSubcategory()); // mode? 4 all, 2 = sp + coop
        size.set(modInfo.getLinks().getFirst().getSize());
        try {
            version.set(modInfo.getVersion());
            dateCreated.set(modInfo.getOriginalCreatedDate().toLocalDate());
        } catch (DateTimeParseException ex) {
            log.warning(ex.getMessage());
        }

        return this;
    }

    public Filter toFilter() {
        return new Filter(getName(), getGame(), getType(), getModes(), isInstalled(), isCompleted(), getDateCreated(), getDateCreated());
    }

    public boolean isGro() {
        return downloadedFile.get() instanceof GroFile;
    }

    public boolean isZip() {
        return downloadedFile.get() instanceof ZipFile;
    }

    public record Filter(String name, Game game, Type type, Mode mode, boolean installed, boolean completed, LocalDate createDateFrom, LocalDate createDateTo) {}

    // ---------------------------- FX Boilerplate --------------------------------

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDownloadedFileName() {
        return downloadedFileName.get();
    }

    public StringProperty downloadedFileNameProperty() {
        return downloadedFileName;
    }

    public void setDownloadedFileName(String downloadedFileName) {
        this.downloadedFileName.set(downloadedFileName);
    }

    public ContentFile getDownloadedFile() {
        return downloadedFile.get();
    }

    public ObjectProperty<ContentFile> downloadedFileProperty() {
        return downloadedFile;
    }

    public void setDownloadedFile(ContentFile downloadedFile) {
        this.downloadedFile.set(downloadedFile);
    }

    public String getVersion() {
        return version.get();
    }

    public StringProperty versionProperty() {
        return version;
    }

    public void setVersion(String version) {
        this.version.set(version);
    }

    public String getOrigin() {
        return origin.get();
    }

    public StringProperty originProperty() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin.set(origin);
    }

    public Game getGame() {
        return game.get();
    }

    public ObjectProperty<Game> gameProperty() {
        return game;
    }

    public void setGame(Game game) {
        this.game.set(game);
    }

    public Type getType() {
        return type.get();
    }

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    public void setType(Type type) {
        this.type.set(type);
    }

    public Mode getModes() {
        return modes.get();
    }

    public ObjectProperty<Mode> modesProperty() {
        return modes;
    }

    public void setModes(Mode modes) {
        this.modes.set(modes);
    }

    public boolean isInstalled() {
        return installed.get();
    }

    public BooleanProperty installedProperty() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed.set(installed);
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed.set(completed);
    }

    public double getRating() {
        return rating.get();
    }

    public DoubleProperty ratingProperty() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating.set(rating);
        //DBManager.getInstance().update(this);
    }

    public long getSize() {
        return size.get();
    }

    public LongProperty sizeProperty() {
        return size;
    }

    public void setSize(long size) {
        this.size.set(size);
    }

    public LocalDateTime getDateAdded() {
        return dateAdded.get();
    }

    public void setDateAdded(LocalDateTime date) {
        dateAdded.set(date);
    }

    public ObjectProperty<LocalDateTime> dateAddedProperty() {
        return dateAdded;
    }

    public LocalDate getDateCreated() {
        return dateCreated.get();
    }

    public ObjectProperty<LocalDate> dateCreatedProperty() {
        return dateCreated;
    }

    public File getInstallFileLocation() {
        return installFileLocation.get();
    }

    public ObjectProperty<File> installFileLocationProperty() {
        return installFileLocation;
    }

    public void setInstallFileLocation(File installFileLocation) {
        this.installFileLocation.set(installFileLocation);
    }

    public LocalDateTime getDateCompleted() {
        return dateCompleted.get();
    }

    public ObjectProperty<LocalDateTime> dateCompletedProperty() {
        return dateCompleted;
    }

    public void setDateCompleted(LocalDateTime dateCompleted) {
        this.dateCompleted.set(dateCompleted);
    }

    public LocalDateTime getDateLastPlayed() {
        return dateLastPlayed.get();
    }

    public ObjectProperty<LocalDateTime> dateLastPlayedProperty() {
        return dateLastPlayed;
    }

    public void setDateLastPlayed(LocalDateTime dateLastPlayed) {
        this.dateLastPlayed.set(dateLastPlayed);
    }
}
