package com.dario.ast.event.folder;

import com.dario.ast.core.domain.Folder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class FolderSelectedEvent extends ComponentEvent<Component> {

  private final Folder folder;

  public FolderSelectedEvent(Folder folder) {
    super(new UI(), false);
    this.folder = folder;
  }
}
