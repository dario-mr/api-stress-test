package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class EnvironmentCreatedEvent extends ComponentEvent<Component> {

  private final Long environmentId;

  public EnvironmentCreatedEvent(Long environmentId) {
    super(new UI(), false);
    this.environmentId = environmentId;
  }
}
