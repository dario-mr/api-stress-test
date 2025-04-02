package com.dario.ast.event;

import com.dario.ast.core.domain.AstRequest;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class ApplyPreRequestEvent extends ComponentEvent<Component> {

  private final AstRequest preRequest;

  public ApplyPreRequestEvent(AstRequest preRequest) {
    super(new UI(), false);
    this.preRequest = preRequest;
  }
}
