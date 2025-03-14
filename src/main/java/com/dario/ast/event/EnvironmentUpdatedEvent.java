package com.dario.ast.event;

import com.dario.ast.core.domain.Environment;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class EnvironmentUpdatedEvent extends ComponentEvent<Component> {

  private final Environment environment;

  public EnvironmentUpdatedEvent(Environment environment) {
    super(new UI(), false);
    this.environment = environment;
  }
}
