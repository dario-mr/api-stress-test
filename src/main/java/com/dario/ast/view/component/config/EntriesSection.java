package com.dario.ast.view.component.config;

import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END;
import static org.springframework.util.StringUtils.hasText;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class EntriesSection extends VerticalLayout {

  private final Map<String, String> entries = new HashMap<>();
  private final Runnable notifyChangesAction;

  public EntriesSection(Runnable notifyChangesAction) {
    this.notifyChangesAction = notifyChangesAction;

    setPadding(false);
    setSpacing(false);
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
      // if key became empty, remove entry
      if (hasText(changeEvent.getOldValue()) && !hasText(changeEvent.getValue()) && entries.size() > 1) {
        entries.remove(changeEvent.getOldValue());
        remove(entryLayout);
        notifyChanges();

        return;
      }

      // if key changed, update its entry (remove it and re-add it)
      entries.remove(changeEvent.getOldValue());
      entries.put(changeEvent.getValue(), valueField.getValue());
      notifyChanges();

      // if there are no empty entries, add one
      if (hasText(changeEvent.getValue()) && !isThereAnEmptyEntry()) {
        addEntry("", "");
      }
    });

    valueField.addValueChangeListener(changeEvent -> {
      entries.put(keyField.getValue(), changeEvent.getValue());
      notifyChanges();
    });

    removeButton.addClickListener(event -> {
      if (entries.size() > 1) {
        entries.remove(keyField.getValue());
        remove(entryLayout);
        notifyChanges();
      }
    });
  }

  public void addEntries(Map<String, String> entries) {
    if (entries == null) {
      return;
    }

    entries.forEach(this::addEntry);
  }

  public void clearEntries() {
    entries.clear();
    removeAll();
  }

  private boolean isThereAnEmptyEntry() {
    return entries.entrySet().stream()
        .anyMatch(entry -> entry.getKey().isEmpty() && entry.getValue().isEmpty());
  }

  private void notifyChanges() {
    notifyChangesAction.run();
  }

  private static TextField createEntryField(String placeHolder, String value) {
    var entryField = new TextField();
    entryField.setPlaceholder(placeHolder);
    entryField.setValue(value);
    entryField.setWidthFull();
    entryField.setMinWidth("0");

    return entryField;
  }
}
