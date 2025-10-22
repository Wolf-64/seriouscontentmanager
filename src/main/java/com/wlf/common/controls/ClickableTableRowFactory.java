package com.wlf.common.controls;

import com.wlf.common.SimpleFileModel;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

public class ClickableTableRowFactory<S extends SimpleFileModel> implements Callback<TableView<S>, TableRow<S>> {
    public TableRow<S> call(TableView<S> param) {
        TableRow<S> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && (!row.isEmpty())) {
                row.getItem().setSelected(!row.getItem().isSelected());
                event.consume();
            }
        });
        return row;
    }
}
