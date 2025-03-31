package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class AstRequestCreatedEvent extends ComponentEvent<Component> {

  private final Long astRequestId;

  public AstRequestCreatedEvent(Long astRequestId) {
    super(new UI(), false);
    this.astRequestId = astRequestId;
  }
}
