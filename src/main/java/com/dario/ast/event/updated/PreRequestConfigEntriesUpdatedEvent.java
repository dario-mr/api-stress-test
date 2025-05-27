package com.dario.ast.event.updated;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class PreRequestConfigEntriesUpdatedEvent extends ComponentEvent<Component> {

  public PreRequestConfigEntriesUpdatedEvent() {
    super(new UI(), false);
  }
}
