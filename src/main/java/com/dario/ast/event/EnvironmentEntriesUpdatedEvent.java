package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class EnvironmentEntriesUpdatedEvent extends ComponentEvent<Component> {

  public EnvironmentEntriesUpdatedEvent() {
    super(new UI(), false);
  }
}
