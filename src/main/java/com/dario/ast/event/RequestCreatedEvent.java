package com.dario.ast.event;

import com.dario.ast.core.domain.Request;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class RequestCreatedEvent extends ComponentEvent<Component> {

  private final Request request;

  public RequestCreatedEvent(Request request) {
    super(new UI(), false);
    this.request = request;
  }
}
