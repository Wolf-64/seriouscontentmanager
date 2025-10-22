package com.wlf.common.controls;

import com.wlf.common.InfoIconProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import static java.util.Objects.requireNonNull;

public class InfoIconTreeTableCell<S, T extends InfoIconProperty> extends TreeTableCell<S, T> {
    private static final String ERROR_CONVERTER_NULL = "Argument 'converter' must not be null";

    public static <S, T extends InfoIconProperty> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn() {
        return param -> new InfoIconTreeTableCell<S, T>();
    }

    public static <S, T extends InfoIconProperty> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(StringConverter<T> converter) {
        return param -> new InfoIconTreeTableCell<S, T>(converter);
    }

    private Subscription subscription;
    private final FontIcon icon;
    private final Tooltip tooltip;
    private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    @SuppressWarnings("unchecked")
    public InfoIconTreeTableCell() {
        this(new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object != null ? String.valueOf(object.getIcon()) : "";
            }

            @Override
            public T fromString(String string) {
                // leave it as is for now
                return null;
            }
        });
    }

    public InfoIconTreeTableCell(StringConverter<T> converter) {
        this.getStyleClass().add("font-icon-table-cell");
        this.icon = new FontIcon();
        this.icon.setIconSize(16);
        this.tooltip = new Tooltip();
        this.tooltip.setFont(Font.font(12));
        setConverter(requireNonNull(converter, ERROR_CONVERTER_NULL));
    }

    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    public final void setConverter(StringConverter<T> converter) {
        converterProperty().set(requireNonNull(converter, ERROR_CONVERTER_NULL));
    }

    public final StringConverter<T> getConverter() {
        return converterProperty().get();
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }

            final TreeTableColumn<S, T> column = getTableColumn();
            ObservableValue<T> observable = column == null ? null : column.getCellObservableValue(getIndex());

            if (observable != null) {
                ChangeListener<T> listener = (v, o, n) -> icon.setIconCode(n.getIcon());
                observable.addListener(listener);
                subscription = () -> observable.removeListener(listener);
                icon.setIconCode(observable.getValue().getIcon());
                if (observable.getValue().getInfo() == null) {
                    setTooltip(null);
                } else {
                    setTooltip(tooltip);
                    tooltip.setText(observable.getValue().getInfo());
                }
            } else if (item != null) {
                icon.setIconCode(item.getIcon());
                tooltip.setText(item.getInfo());
                if (item.getInfo() == null) {
                    setTooltip(null);
                } else {
                    setTooltip(tooltip);
                    tooltip.setText(item.getInfo());
                }
            }

            setGraphic(icon);
            setAlignment(Pos.CENTER);
        }
    }

    private interface Subscription {
        void unsubscribe();
    }
}
