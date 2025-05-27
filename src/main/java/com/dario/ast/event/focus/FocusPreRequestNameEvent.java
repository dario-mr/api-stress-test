package com.dario.ast.event.focus;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class FocusPreRequestNameEvent extends ComponentEvent<Component> {

  public FocusPreRequestNameEvent() {
    super(new UI(), false);
  }
}
