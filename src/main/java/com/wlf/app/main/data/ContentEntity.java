package com.wlf.app.main.data;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.wlf.app.preferences.Config;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class ContentEntity {
    public static final Logger log = Logger.getLogger(ContentModel.class.getSimpleName());

    private static final Config config = Config.getInstance();

    /** Name of the map/mod/character model */
    private String name;
    /** Filename of either the .gro or zip-file that was downloaded */
    private String downloadedFileName;
    /** Game this content works with/was made for */
    private Game game;
    /** Type of content (map/mod/character) */
    private Type type;
    /** Game modes this content works with (SP/Co-op/DM) */
    private Mode modes;
    /** Whether this content is currently installed. */
    private Boolean installed;
    /** Whether the content was manually marked as completed */
    private Boolean completed;
    /** The date this content has been added/downloaded to the library */
    private LocalDateTime dateAdded;
    /** The original creation date of this content */
    private LocalDate dateCreated;
    /** The original creation date of this content */
    private LocalDateTime dateCompleted;
    /** The original creation date of this content */
    private LocalDateTime dateLastPlayed;
    /** The rating that has been given to the content */
    private Double rating;
    /** Absolute path of the content's file (gro or zip) after download */
    private ContentFile downloadedFile;
    /** Absolute path of the content's file (gro) after installing */
    private File installFileLocation;
    /** All files that have been extracted from zipped content */
    private final List<File> installedFiles = new ArrayList<>();
    /** Either the URL this content has been downloaded from, or 'local' if it has been added manually */
    private String origin;
    /** The content's version */
    private String version;
    /** Total file size of the content (gro or zip) */
    private Long size;

    /** GRO Repository file/mod id */
    private String repoId;
    /** Database ID */
    @Id
    @GeneratedValue
    private Long id;

    public ContentEntity() {    }
}