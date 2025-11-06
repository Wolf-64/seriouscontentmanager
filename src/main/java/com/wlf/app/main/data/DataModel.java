package com.wlf.app.main.data;

import com.wlf.common.BaseModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

public class DataModel extends BaseModel {
    @Getter
    private final ObservableList<ContentModel> content = FXCollections.observableArrayList();

}
