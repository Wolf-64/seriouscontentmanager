package com.wlf.common.controls;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.beans.binding.Bindings;
import javafx.scene.control.CheckBox;
import javafx.util.StringConverter;
import org.controlsfx.control.Rating;

/**
 * A class containing a {@link TableCell} implementation that draws a
 * {@link CheckBox} node inside the cell, optionally with a label to indicate
 * what the checkbox represents.
 *
 * <p>By default, the RatingTableCell is rendered with a CheckBox centred in
 * the TableColumn. If a label is required, it is necessary to provide a
 * non-null StringConverter instance to the
 * {@link #RatingTableCell(Callback, StringConverter)} constructor.
 *
 * <p>To construct an instance of this class, it is necessary to provide a
 * {@link Callback} that, given an object of type T, will return an
 * {@code ObservableProperty<Double>} that represents whether the given item is
 * selected or not. This ObservableValue will be bound bidirectionally (meaning
 * that the CheckBox in the cell will set/unset this property based on user
 * interactions, and the CheckBox will reflect the state of the ObservableValue,
 * if it changes externally).
 *
 * <p>Note that the RatingTableCell renders the CheckBox 'live', meaning that
 * the CheckBox is always interactive and can be directly toggled by the user.
 * This means that it is not necessary that the cell enter its
 * {@link #editingProperty() editing state} (usually by the user double-clicking
 * on the cell). A side-effect of this is that the usual editing callbacks
 * (such as {@link javafx.scene.control.TableColumn#onEditCommitProperty() on edit commit})
 * will <strong>not</strong> be called. If you want to be notified of changes,
 * it is recommended to directly observe the boolean properties that are
 * manipulated by the CheckBox.</p>
 *
 * @param <T> The type of the elements contained within the TableColumn.
 * @since JavaFX 2.2
 */
public class RatingTableCell<S,T> extends TableCell<S,T> {

    /* *************************************************************************
     *                                                                         *
     * Static cell factories                                                   *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a cell factory for use in a {@link TableColumn} cell factory.
     * This method requires that the TableColumn be of type {@link Double}.
     *
     * <p>When used in a TableColumn, the CheckBoxCell is rendered with a
     * CheckBox centered in the column.
     *
     * <p>The {@code ObservableValue<Double>} contained within each cell in the
     * column will be bound bidirectionally. This means that the  CheckBox in
     * the cell will set/unset this property based on user interactions, and the
     * CheckBox will reflect the state of the {@code ObservableValue<Double>},
     * if it changes externally).
     *
     * @param <S> The type of the TableView generic type
     * @param column The TableColumn of type Double
     * @return A {@link Callback} that will return a {@link TableCell} that is
     *      able to work on the type of element contained within the TableColumn.
     */
    public static <S> Callback<TableColumn<S,Double>, TableCell<S,Double>> forTableColumn(
            final TableColumn<S, Double> column) {
        return forTableColumn(null, null);
    }

    /**
     * Creates a cell factory for use in a {@link TableColumn} cell factory.
     * This method requires that the TableColumn be of type
     * {@code ObservableValue<Double>}.
     *
     * <p>When used in a TableColumn, the CheckBoxCell is rendered with a
     * CheckBox centered in the column.
     *
     * @param <S> The type of the TableView generic type
     * @param <T> The type of the elements contained within the {@link TableColumn}
     *      instance.
     * @param getSelectedProperty A Callback that, given an object of
     *      type {@code TableColumn<S,T>}, will return an
     *      {@code ObservableValue<Double>}
     *      that represents whether the given item is selected or not. This
     *      {@code ObservableValue<Double>} will be bound bidirectionally
     *      (meaning that the CheckBox in the cell will set/unset this property
     *      based on user interactions, and the CheckBox will reflect the state of
     *      the {@code ObservableValue<Double>}, if it changes externally).
     * @return A {@link Callback} that will return a {@link TableCell} that is
     *      able to work on the type of element contained within the TableColumn.
     */
    public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
            final Callback<Integer, ObservableValue<Double>> getSelectedProperty) {
        return forTableColumn(getSelectedProperty, null);
    }

    /**
     * Creates a cell factory for use in a {@link TableColumn} cell factory.
     * This method requires that the TableColumn be of type
     * {@code ObservableValue<Double>}.
     *
     * <p>When used in a TableColumn, the CheckBoxCell is rendered with a
     * CheckBox centered in the column.
     *
     * @param <S> The type of the TableView generic type
     * @param <T> The type of the elements contained within the {@link TableColumn}
     *      instance.
     * @param getSelectedProperty A Callback that, given an object of
     *      type {@code TableColumn<S,T>}, will return an
     *      {@code ObservableValue<Double>}
     *      that represents whether the given item is selected or not. This
     *      {@code ObservableValue<Double>} will be bound bidirectionally
     *      (meaning that the CheckBox in the cell will set/unset this property
     *      based on user interactions, and the CheckBox will reflect the state of
     *      the {@code ObservableValue<Double>}, if it changes externally).
     * @param showLabel In some cases, it may be desirable to show a label in
     *      the TableCell beside the {@link CheckBox}. By default a label is not
     *      shown, but by setting this to true the item in the cell will also
     *      have toString() called on it. If this is not the desired behavior,
     *      consider using
     *      {@link #forTableColumn(javafx.util.Callback, javafx.util.StringConverter) },
     *      which allows for you to provide a callback that specifies the label for a
     *      given row item.
     * @return A {@link Callback} that will return a {@link TableCell} that is
     *      able to work on the type of element contained within the TableColumn.
     */
    public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
            final Callback<Integer, ObservableValue<Double>> getSelectedProperty,
            final boolean showLabel) {
        StringConverter<T> converter = ! showLabel ?
                null : null; // TODO CellUtils.<T>defaultStringConverter();
        return forTableColumn(getSelectedProperty, converter);
    }

    /**
     * Creates a cell factory for use in a {@link TableColumn} cell factory.
     * This method requires that the TableColumn be of type
     * {@code ObservableValue<Double>}.
     *
     * <p>When used in a TableColumn, the CheckBoxCell is rendered with a
     * CheckBox centered in the column.
     *
     * @param <S> The type of the TableView generic type
     * @param <T> The type of the elements contained within the {@link TableColumn}
     *      instance.
     * @param getSelectedProperty A Callback that, given an object of type
     *      {@code TableColumn<S,T>}, will return an
     *      {@code ObservableValue<Double>} that represents whether the given
     *      item is selected or not. This {@code ObservableValue<Double>} will
     *      be bound bidirectionally (meaning that the CheckBox in the cell will
     *      set/unset this property based on user interactions, and the CheckBox
     *      will reflect the state of the {@code ObservableValue<Double>}, if
     *      it changes externally).
     * @param converter A StringConverter that, give an object of type T, will return a
     *      String that can be used to represent the object visually. The default
     *      implementation in {@link #forTableColumn(Callback, Double)} (when
     *      showLabel is true) is to simply call .toString() on all non-null
     *      items (and to just return an empty string in cases where the given
     *      item is null).
     * @return A {@link Callback} that will return a {@link TableCell} that is
     *      able to work on the type of element contained within the TableColumn.
     */
    public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
            final Callback<Integer, ObservableValue<Double>> getSelectedProperty,
            final StringConverter<T> converter) {
        return list -> new RatingTableCell<>(getSelectedProperty, converter);
    }



    /* *************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/
    private final Rating rating;

    private boolean showLabel;

    private ObservableValue<Double> value;



    /* *************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a default CheckBoxTableCell.
     */
    public RatingTableCell() {
        this(null, null);
    }

    /**
     * Creates a default CheckBoxTableCell with a custom {@link Callback} to
     * retrieve an ObservableValue for a given cell index.
     *
     * @param getSelectedProperty A {@link Callback} that will return an {@link
     *      ObservableValue} given an index from the TableColumn.
     */
    public RatingTableCell(
            final Callback<Integer, ObservableValue<Double>> getSelectedProperty) {
        this(getSelectedProperty, null);
    }

    /**
     * Creates a CheckBoxTableCell with a custom string converter.
     *
     * @param getSelectedProperty A {@link Callback} that will return a {@link
     *      ObservableValue} given an index from the TableColumn.
     * @param converter A StringConverter that, given an object of type T, will return a
     *      String that can be used to represent the object visually.
     */
    public RatingTableCell(
            final Callback<Integer, ObservableValue<Double>> getSelectedProperty,
            final StringConverter<T> converter) {
        // we let getSelectedProperty be null here, as we can always defer to the
        // TableColumn
        this.getStyleClass().add("rating-table-cell");

        this.rating = new Rating();

        // by default the graphic is null until the cell stops being empty
        setGraphic(null);

        setSelectedStateCallback(getSelectedProperty);
        setConverter(converter);

//        // alignment is styleable through css. Calling setAlignment
//        // makes it look to css like the user set the value and css will not
//        // override. Initializing alignment by calling set on the
//        // CssMetaData ensures that css will be able to override the value.
//        final CssMetaData prop = CssMetaData.getCssMetaData(alignmentProperty());
//        prop.set(this, Pos.CENTER);


    }


    /* *************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // --- converter
    private ObjectProperty<StringConverter<T>> converter =
            new SimpleObjectProperty<>(this, "converter") {
                protected void invalidated() {
                    updateShowLabel();
                }
            };

    /**
     * The {@link StringConverter} property.
     * @return the {@link StringConverter} property
     */
    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    /**
     * Sets the {@link StringConverter} to be used in this cell.
     * @param value the {@link StringConverter} to be used in this cell
     */
    public final void setConverter(StringConverter<T> value) {
        converterProperty().set(value);
    }

    /**
     * Returns the {@link StringConverter} used in this cell.
     * @return the {@link StringConverter} used in this cell
     */
    public final StringConverter<T> getConverter() {
        return converterProperty().get();
    }



    // --- selected state callback property
    private ObjectProperty<Callback<Integer, ObservableValue<Double>>>
            selectedStateCallback =
            new SimpleObjectProperty<>(
                    this, "selectedStateCallback");

    /**
     * Property representing the {@link Callback} that is bound to by the
     * CheckBox shown on screen.
     * @return the property representing the {@link Callback} that is bound to
     * by the CheckBox shown on screen
     */
    public final ObjectProperty<Callback<Integer, ObservableValue<Double>>> selectedStateCallbackProperty() {
        return selectedStateCallback;
    }

    /**
     * Sets the {@link Callback} that is bound to by the CheckBox shown on screen.
     * @param value the {@link Callback} that is bound to by the CheckBox shown
     * on screen
     */
    public final void setSelectedStateCallback(Callback<Integer, ObservableValue<Double>> value) {
        selectedStateCallbackProperty().set(value);
    }

    /**
     * Returns the {@link Callback} that is bound to by the CheckBox shown on screen.
     * @return the {@link Callback} that is bound to by the CheckBox shown on screen
     */
    public final Callback<Integer, ObservableValue<Double>> getSelectedStateCallback() {
        return selectedStateCallbackProperty().get();
    }



    /* *************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            StringConverter<T> c = getConverter();

            if (showLabel) {
                setText(c.toString(item));
            }
            setGraphic(rating);

            if (value instanceof Property<?> property && property instanceof DoubleProperty) {
                rating.ratingProperty().unbindBidirectional((DoubleProperty) property);
            }
            ObservableValue<?> obsValue = getSelectedProperty();
            if (obsValue instanceof DoubleProperty) {
                value = (ObservableValue<Double>) obsValue;
                rating.ratingProperty().bindBidirectional((DoubleProperty)((Property<?>) value));
            }

            rating.disableProperty().bind(Bindings.not(
                    getTableView().editableProperty().and(
                            getTableColumn().editableProperty()).and(
                            editableProperty())
            ));
        }
    }



    /* *************************************************************************
     *                                                                         *
     * Private implementation                                                  *
     *                                                                         *
     **************************************************************************/

    private void updateShowLabel() {
        this.showLabel = converter != null;
        //this.rating.setAlignment(showLabel ? Pos.CENTER_LEFT : Pos.CENTER);
    }

    private ObservableValue<?> getSelectedProperty() {
        return getSelectedStateCallback() != null ?
                getSelectedStateCallback().call(getIndex()) :
                getTableColumn().getCellObservableValue(getIndex());
    }
}
