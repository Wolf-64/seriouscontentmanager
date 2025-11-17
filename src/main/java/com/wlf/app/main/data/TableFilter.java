package com.wlf.app.main.data;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TableFilter {
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<Game> game = new SimpleObjectProperty<>();
    private final ObjectProperty<Type> type = new SimpleObjectProperty<>();
    private final ObjectProperty<Mode> modes = new SimpleObjectProperty<>();
    private final BooleanProperty installed = new SimpleBooleanProperty();
    private final BooleanProperty completed = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> dateAdded = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateCreatedFrom = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateCreatedTo = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> dateCompleted = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> dateLastPlayed = new SimpleObjectProperty<>();
    private final DoubleProperty rating = new SimpleDoubleProperty();

    // ------------------------------------- FX Boilerplate ---------------------------------

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
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

    public LocalDateTime getDateAdded() {
        return dateAdded.get();
    }

    public ObjectProperty<LocalDateTime> dateAddedProperty() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded.set(dateAdded);
    }

    public LocalDate getDateCreatedFrom() {
        return dateCreatedFrom.get();
    }

    public ObjectProperty<LocalDate> dateCreatedFromProperty() {
        return dateCreatedFrom;
    }

    public void setDateCreatedFrom(LocalDate dateCreatedFrom) {
        this.dateCreatedFrom.set(dateCreatedFrom);
    }

    public LocalDate getDateCreatedTo() {
        return dateCreatedTo.get();
    }

    public ObjectProperty<LocalDate> dateCreatedToProperty() {
        return dateCreatedTo;
    }

    public void setDateCreatedTo(LocalDate dateCreatedTo) {
        this.dateCreatedTo.set(dateCreatedTo);
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

    public double getRating() {
        return rating.get();
    }

    public DoubleProperty ratingProperty() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating.set(rating);
    }
}
