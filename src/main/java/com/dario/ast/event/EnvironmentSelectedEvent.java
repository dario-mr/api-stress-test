package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class EnvironmentSelectedEvent extends ComponentEvent<Component> {

  public EnvironmentSelectedEvent() {
    super(new UI(), false);
  }
}
