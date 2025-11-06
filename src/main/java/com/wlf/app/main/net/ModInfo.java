package com.wlf.app.main.net;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wlf.app.main.data.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModInfo {
    /** GroRepository content ID. */
    private long id;
    /** Date the content has been created on GroRepo */
    private LocalDateTime createdDate;
    /** Date the content has been updated on GroRepo */
    private LocalDateTime changedDate;
    /** Actual map/mod title */
    private String title;
    /** Alternative ID string. Title without whitespace, only ASCII characters (i.e. no Cyrillic) */
    private String transliteratedTitle;
    /** i18n description string. Format: i18n{"en":"english description","ru":"russian description" */
    private String description;
    /** List of author objects. */
    private List<Author> authors;
    /** Content type, e.g. map, mod etc. */
    private Type type;
    /** Content version, if applicable */
    private String version;
    /** Compatible the game the content works with */
    private Game game;
    /** List of download information */
    private List<Link> links;
    /** List of media information, screenshots or video */
    private List<Media> media;
    /** Rating. Unused? */
    private Double rating;
    /** Supported game mode */
    private Mode subcategory;
    /** Tags used for filtering within GroRepository */
    private List<Integer> tagIds;
    /** Unknown */
    private String modFolder;
    /** Unknown */
    private Integer downloadCount;
    /** Date the map/mod has originally been created */
    private LocalDateTime originalCreatedDate;
    /** Unknown */
    private String fields;

    public boolean isMultilingual() {
        return links != null && links.size() > 1;
    }

    @Data
    public static class Author {
        /** GroRepository content ID */
        private long id;
        /** Date the content has been created on GroRepo */
        private LocalDateTime createdDate;
        /** Date the content has been updated on GroRepo */
        private LocalDateTime changedDate;
        /** Name of the author or translator */
        private String name;
        /** Alternative ID string. Title without whitespace, only ASCII characters (i.e. no Cyrillic) */
        private String transliteratedName;
        /** History of usernames for that author, if applicable */
        private String previousNames;
        /** Author description, or "about" text, if applicable */
        private String description;
        /** Presumably content ID string of the author's avatar image */
        private String avatar;
        /** Unknown */
        private String fields;
        /** Presumably list of content IDs the author created. Unused as of 11/01/25 */
        private List<Long> mods;
        /** Author type for a given mod/map. Can be original author or translator */
        private AuthorType modAuthorType;
    }

    @Data
    public static class Link {
        /** Actual GroRepository content link. Only valid for media. Mod/map links will be requested separately */
        private String link;
        /** Media preview file link. Presumably scaled down images or compressed video */
        private String previewLink;
        /** Content size of a given link's target file */
        private long size;
        /** Language of a mod/map */
        private ContentLanguage type;
        /** Presumably order of appearance of the download links for different languages */
        private int sortOrder;
    }

    @Data
    public static class Media extends Link { }
}
