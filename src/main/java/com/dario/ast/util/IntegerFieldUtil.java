package com.dario.ast.util;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.textfield.IntegerField;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IntegerFieldUtil {

    public static HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<IntegerField, Integer>> integerValidationListener(
            IntegerField field, int minValue) {
        return event -> {
            if (event.getValue() == null) {
                field.setValue(event.getOldValue());
            }
            if (event.getValue() < minValue) {
                field.setValue(minValue);
            }
        };
    }

}
