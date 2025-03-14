package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class FocusEnvNameEvent extends ComponentEvent<Component> {

  public FocusEnvNameEvent() {
    super(new UI(), false);
  }
}
