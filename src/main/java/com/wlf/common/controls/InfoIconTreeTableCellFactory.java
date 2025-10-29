package com.wlf.common.controls;

import com.wlf.common.InfoIconProperty;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class InfoIconTreeTableCellFactory<S, T extends InfoIconProperty> implements Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> {
    public InfoIconTreeTableCell<S, T> call(TreeTableColumn<S, T> param) {
        return new InfoIconTreeTableCell<>();
    }
}

