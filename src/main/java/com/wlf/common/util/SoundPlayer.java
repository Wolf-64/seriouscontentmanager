package com.wlf.common.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Setter;

/**
 * Simple sound player encapsulating JavaFX {@link Media} and {@link MediaPlayer} classes for quick and easy use.
 */
public class SoundPlayer {
    private MediaPlayer player;
    private final Media defaultSound;
    @Setter
    private BooleanProperty mute;
    @Setter
    private DoubleProperty volume;

    /**
     * Creates a sound player which can be played multiple times (auto-rewind).
     * @param defaultSoundFile must represent a valid file URI (usually via getResource().toExternalForm())
     * @param mute property to bind player mute, e.g. configuration properties
     */
    public SoundPlayer(String defaultSoundFile, BooleanProperty mute, DoubleProperty volume) {
        defaultSound = new Media(defaultSoundFile);
        player = new MediaPlayer(defaultSound);
        this.mute = mute;
        this.volume = volume;

        initPlayer();
    }

    private void initPlayer() {
        player.setOnEndOfMedia(() -> {
            player.stop();
            player.seek(Duration.ZERO); // rewind that
        });

        if (this.mute != null) {
            player.muteProperty().bind(this.mute);
        }

        if (this.volume != null) {
            player.volumeProperty().bind(this.volume);
        }
    }

    public void play() {
        player.play();
    }

    public void setCustomSound(String filePath) throws MediaException {
        if (filePath != null) {
            String externalFilePath = Utils.createExternalPathString(filePath);
            if (externalFilePath != null) {
                player.stop();
                try {
                    Media customSound = new Media(externalFilePath);
                    player = new MediaPlayer(customSound);
                    initPlayer();
                } catch (MediaException ex) {
                    // back to built-in default songs which we know are working
                    useDefaultSound();
                    throw ex;
                }
            }
        }
    }

    public void useDefaultSound() {
        player = new MediaPlayer(defaultSound);
        initPlayer();
    }
}
