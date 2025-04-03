package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class PreRequestCreatedEvent extends ComponentEvent<Component> {

  private final Long preRequestId;

  public PreRequestCreatedEvent(Long preRequestId) {
    super(new UI(), false);
    this.preRequestId = preRequestId;
  }
}
