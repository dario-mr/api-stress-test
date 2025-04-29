package com.dario.ast.event.folder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class FocusFolderNameEvent extends ComponentEvent<Component> {

  public FocusFolderNameEvent() {
    super(new UI(), false);
  }
}
