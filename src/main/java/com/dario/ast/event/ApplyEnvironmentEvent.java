package com.dario.ast.event;

import com.dario.ast.core.domain.Environment;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class ApplyEnvironmentEvent extends ComponentEvent<Component> {

  private final Environment environment;

  public ApplyEnvironmentEvent(Environment environment) {
    super(new UI(), false);
    this.environment = environment;
  }
}
