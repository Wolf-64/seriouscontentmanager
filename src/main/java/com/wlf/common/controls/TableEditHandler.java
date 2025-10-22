package com.wlf.common.controls;

import javafx.beans.property.StringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;

import java.util.function.Function;

public class TableEditHandler {
    public static <S> void bindEditableTextColumn(TableColumn<S, String> column, Function<S, StringProperty> propertyGetter) {
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setOnEditCommit(e -> propertyGetter.apply(e.getRowValue()).set(e.getNewValue()));
    }

    public static <S> void bindEditableTreeTextColumn(TreeTableColumn<S, String> column, Function<S, StringProperty> propertyGetter) {
        column.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        column.setOnEditCommit(e -> propertyGetter.apply(e.getRowValue().getValue()).set(e.getNewValue()));
    }
}
