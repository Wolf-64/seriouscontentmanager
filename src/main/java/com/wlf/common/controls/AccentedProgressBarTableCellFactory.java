package com.wlf.common.controls;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class AccentedProgressBarTableCellFactory<S> implements Callback<TableColumn<S, Double>, TableCell<S, Double>> {
    public TableCell<S, Double> call(TableColumn<S, Double> param) {
        return new AccentedProgressBarTableCell<>();
    }
}
