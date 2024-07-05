package com.dario.ast.view.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static com.dario.ast.util.EventUtil.refreshPreview;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END;
import static org.springframework.util.StringUtils.hasText;

@Getter
public class EntriesSection extends VerticalLayout {

    private final Map<String, String> entries = new HashMap<>();

    public EntriesSection() {
        setPadding(false);
    }

    public void addEntry(String key, String value) {
        entries.put(key, value);

        // add entry to the UI
        var keyField = createEntryField("Key", key);
        var valueField = createEntryField("Value", value);

        var removeButton = new Button();
        removeButton.setIcon(TRASH.create());

        var entryLayout = new HorizontalLayout(keyField, valueField, removeButton);
        entryLayout.setWidthFull();
        entryLayout.setVerticalComponentAlignment(END, removeButton);
        add(entryLayout);

        // handle UI actions
        keyField.addValueChangeListener(changeEvent -> {
            entries.remove(changeEvent.getOldValue());
            entries.put(changeEvent.getValue(), valueField.getValue());
            refreshPreview();

            if (hasText(changeEvent.getValue()) && !isThereAnEmptyEntry()) {
                addEntry("", "");
            }
        });

        valueField.addValueChangeListener(changeEvent -> {
            entries.put(keyField.getValue(), changeEvent.getValue());
            refreshPreview();
        });

        removeButton.addClickListener(event -> {
            if (entries.size() > 1) {
                entries.remove(keyField.getValue());
                remove(entryLayout);
                refreshPreview();
            }
        });
    }

    public void addEntries(Map<String, String> entries) {
        if (entries == null) {
            return;
        }

        entries.forEach(this::addEntry);
    }

    private boolean isThereAnEmptyEntry() {
        return entries.entrySet().stream()
                .anyMatch(entry -> entry.getKey().isEmpty() && entry.getValue().isEmpty());
    }

    private static TextField createEntryField(String placeHolder, String value) {
        var entryField = new TextField();
        entryField.setPlaceholder(placeHolder);
        entryField.setValue(value);
        entryField.setWidthFull();
        entryField.setMaxWidth("15em");
        entryField.setMinWidth("0");

        return entryField;
    }
}
