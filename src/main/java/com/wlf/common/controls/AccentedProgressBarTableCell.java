package com.wlf.common.controls;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * A class containing a {@link TableCell} implementation that draws a
 * {@link AccentedProgressBar} node inside the cell.
 *
 * @param <S> The type of the elements contained within the TableView.
 * @since I've done it myself, thanks for not just letting me inherit it.
 */
public class AccentedProgressBarTableCell<S> extends TableCell<S, Double> {

    /* *************************************************************************
     *                                                                         *
     * Static cell factories                                                   *
     *                                                                         *
     **************************************************************************/

    /**
     * Provides a {@link AccentedProgressBar} that allows easy visualisation of a Number
     * value as it proceeds from 0.0 to 1.0. If the value is -1, the progress
     * bar will appear indeterminate.
     *
     * @param <S> The type of the TableView generic type
     * @return A {@link Callback} that can be inserted into the
     *      {@link TableColumn#cellFactoryProperty() cell factory property} of a
     *      TableColumn, that enables visualisation of a Number as it progresses
     *      from 0.0 to 1.0.
     */
    public static <S> Callback<TableColumn<S,Double>, TableCell<S,Double>> forTableColumn() {
        return param -> new AccentedProgressBarTableCell<S>();
    }



    /* *************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private final AccentedProgressBar progressBar;

    private ObservableValue<Double> observable;



    /* *************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a default {@link AccentedProgressBarTableCell} instance
     */
    public AccentedProgressBarTableCell() {
        this.getStyleClass().add("progress-bar-table-cell");

        this.progressBar = new AccentedProgressBar();
        this.progressBar.setMaxWidth(Double.MAX_VALUE);
    }



    /* *************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override public void updateItem(Double item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            progressBar.progressProperty().unbind();

            final TableColumn<S,Double> column = getTableColumn();
            observable = column == null ? null : column.getCellObservableValue(getIndex());

            if (observable != null) {
                progressBar.progressProperty().bind(observable);
            } else if (item != null) {
                progressBar.setProgress(item);
            }

            setGraphic(progressBar);
        }
    }
}
