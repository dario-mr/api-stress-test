package com.dario.ast.view.component.sidebar.request;

import static com.dario.ast.core.domain.RequestType.REQUEST;
import static com.dario.ast.core.domain.RunParams.defaultRunParams;
import static com.dario.ast.util.EventUtil.requestCreated;
import static org.springframework.http.HttpMethod.GET;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Request;
import com.dario.ast.core.service.RequestService;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UIScope
@SpringComponent
public class CreateRequestButton extends Button {

  private final RequestService requestService;
  private final AppState appState;

  public CreateRequestButton(RequestService requestService, AppState appState) {
    this.requestService = requestService;
    this.appState = appState;

    addClassName("create-button");
    setWidthFull();
    setText("+");

    addClickListener(event -> createRequest());
  }

  private void createRequest() {
    var currentUserId = appState.getCurrentUser().getId();
    var configParams = defaultConfigParams(currentUserId);
    var runParams = defaultRunParams();
    var request = new Request(configParams, runParams, null);

    try {
      var requestId = requestService.create(request);
      requestCreated(requestId);
    } catch (Exception ex) {
      log.error("Error creating new Request", ex);
      ErrorNotification.show("Error creating Request");
      throw ex;
    }
  }

  private static ConfigParams defaultConfigParams(Long userId) {
    return ConfigParams.builder()
        .requestName("New Request")
        .userId(userId)
        .uri("")
        .method(GET)
        .requestBody("")
        .requestType(REQUEST)
        .active(true)
        .build();
  }

}