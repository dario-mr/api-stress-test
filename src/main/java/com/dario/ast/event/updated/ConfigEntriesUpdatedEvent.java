package com.dario.ast.event.updated;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class ConfigEntriesUpdatedEvent extends ComponentEvent<Component> {

  public ConfigEntriesUpdatedEvent() {
    super(new UI(), false);
  }
}
