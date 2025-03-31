package com.dario.ast.view.component.common.entries;

import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap.NOWRAP;
import static org.springframework.util.StringUtils.hasText;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public class EntriesSection<V extends EntryValue> extends VerticalLayout {

  private final Map<String, V> entries = new LinkedHashMap<>();
  private final Runnable notifyChangesAction;
  private final Supplier<V> defaultValueSupplier;

  public EntriesSection(Runnable notifyChangesAction, Supplier<V> defaultValueSupplier) {
    this.notifyChangesAction = notifyChangesAction;
    this.defaultValueSupplier = defaultValueSupplier;

    setPadding(false);
    setSpacing(false);
  }

  public void addEntry(String key, V value) {
    entries.put(key, value);

    // add entry to the UI
    var keyField = createEntryField("Key", key);
    var valueField = createEntryField("Value", value.getValue());

    var removeButton = new Button();
    removeButton.setIcon(TRASH.create());

    var entryLayout = new FlexLayout(keyField, valueField, removeButton);
    entryLayout.setWidthFull();
    entryLayout.setFlexWrap(NOWRAP);
    entryLayout.setFlexGrow(1, keyField);
    entryLayout.setFlexGrow(3, valueField);
    entryLayout.getStyle().set("gap", "var(--lumo-space-s)");
    add(entryLayout);

    // key changed listener
    keyField.addValueChangeListener(changeEvent -> {
      // if key became empty, remove entry
      if (hasText(changeEvent.getOldValue()) && !hasText(changeEvent.getValue()) && entries.size() > 1) {
        entries.remove(changeEvent.getOldValue());
        remove(entryLayout);
        notifyChanges();

        return;
      }

      // if key changed, remove it and re-add it
      var oldValue = entries.get(changeEvent.getOldValue());
      entries.remove(changeEvent.getOldValue());
      entries.put(changeEvent.getValue(), oldValue);
      notifyChanges();

      // if there are no empty entries, add one
      if (hasText(changeEvent.getValue()) && !isThereAnEmptyEntry()) {
        addEntry("", defaultValueSupplier.get());
      }
    });

    // value changed listener
    valueField.addValueChangeListener(changeEvent -> {
      var currentValue = entries.get(keyField.getValue());
      currentValue.setValue(changeEvent.getValue());
      entries.put(keyField.getValue(), currentValue);
      notifyChanges();
    });

    // remove button listener
    removeButton.addClickListener(event -> {
      if (entries.size() > 1) {
        entries.remove(keyField.getValue());
        remove(entryLayout);
        notifyChanges();
      }
    });
  }

  public void addEntries(Map<String, V> entries) {
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
        .anyMatch(entry -> entry.getKey().isEmpty() && entry.getValue().getValue().isEmpty());
  }

  private void notifyChanges() {
    notifyChangesAction.run();
  }

  private static TextField createEntryField(String placeHolder, String value) {
    var entryField = new TextField();
    entryField.setPlaceholder(placeHolder);
    entryField.setValue(value);

    return entryField;
  }
}
