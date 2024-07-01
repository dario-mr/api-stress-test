package com.dario.ast.util;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END;
import static org.springframework.util.StringUtils.hasText;

/**
 * Utility class to handle UI entry elements.
 * An entry is defined as a horizontal layout containing a "key" text field, "value" test field, "delete" button.
 */
@UtilityClass
public class EntryUtil {

    public static void addEntry(VerticalLayout target) {
        var keyField = new TextField();
        var valueField = new TextField();
        var removeButton = new Button();
        var entry = new HorizontalLayout(keyField, valueField, removeButton);
        entry.setVerticalComponentAlignment(END, removeButton);

        valueField.setPlaceholder("Value");

        keyField.setPlaceholder("Key");
        keyField.addValueChangeListener(changeEvent -> {
            if (hasText(changeEvent.getValue()) && !isThereAnEmptyEntry(target)) {
                addEntry(target);
            }
        });

        removeButton.setIcon(TRASH.create());
        removeButton.addClickListener(event -> {
            if (target.getChildren().count() > 1) {
                target.remove(entry);
            }
        });

        target.add(entry);
    }

    public static boolean isThereAnEmptyEntry(VerticalLayout target) {
        return target.getChildren().anyMatch(component -> {
            var layout = (HorizontalLayout) component;
            var keyField = (TextField) layout.getComponentAt(0);
            var valueField = (TextField) layout.getComponentAt(1);

            var key = keyField.getValue();
            var value = valueField.getValue();

            return key.isEmpty() && value.isEmpty();
        });
    }

    public static Map<String, String> collectMap(VerticalLayout target) {
        var map = new HashMap<String, String>();

        target.getChildren().forEach(component -> {
            var layout = (HorizontalLayout) component;
            var keyField = (TextField) layout.getComponentAt(0);
            var valueField = (TextField) layout.getComponentAt(1);

            var key = keyField.getValue();
            var value = valueField.getValue();
            if (!key.isEmpty() && !value.isEmpty()) {
                map.put(key, value);
            }
        });

        return map;
    }
}
