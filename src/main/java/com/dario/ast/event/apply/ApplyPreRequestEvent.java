package com.dario.ast.event.apply;

import com.dario.ast.core.domain.ConfigParams;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class ApplyPreRequestEvent extends ComponentEvent<Component> {

  private final ConfigParams preRequestConfigParams;

  public ApplyPreRequestEvent(ConfigParams preRequestConfigParams) {
    super(new UI(), false);
    this.preRequestConfigParams = preRequestConfigParams;
  }
}
