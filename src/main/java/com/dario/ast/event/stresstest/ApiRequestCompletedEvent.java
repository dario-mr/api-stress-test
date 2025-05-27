package com.dario.ast.event.stresstest;

import com.dario.ast.proxy.api.dto.ApiResponse;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class ApiRequestCompletedEvent extends ComponentEvent<Component> {

  private final ApiResponse apiResponse;

  public ApiRequestCompletedEvent(ApiResponse apiResponse) {
    super(new UI(), false);
    this.apiResponse = apiResponse;
  }
}
