package com.wlf.common.controls;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import org.kordamp.ikonli.javafx.FontIconTableCell;

public class FontIconTableCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {
    public FontIconTableCell<S, T> call(TableColumn<S, T> param) {
        return new FontIconTableCell<>();
    }
}

