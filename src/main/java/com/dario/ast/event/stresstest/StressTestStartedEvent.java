package com.dario.ast.event.stresstest;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class StressTestStartedEvent extends ComponentEvent<Component> {

  public StressTestStartedEvent() {
    super(new UI(), false);
  }
}
