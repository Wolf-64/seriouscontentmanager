package com.wlf.common.controls;

import com.wlf.common.InfoIconProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class InfoIconTableCellFactory<S, T extends InfoIconProperty> implements Callback<TableColumn<S, T>, TableCell<S, T>> {
    public InfoIconTableCell<S, T> call(TableColumn<S, T> param) {
        return new InfoIconTableCell<>();
    }
}

