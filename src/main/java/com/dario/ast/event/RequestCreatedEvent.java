package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class RequestCreatedEvent extends ComponentEvent<Component> {

  private final Long requestId;

  public RequestCreatedEvent(Long requestId) {
    super(new UI(), false);
    this.requestId = requestId;
  }
}
