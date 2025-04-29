package com.dario.ast.view.component.sidebar.prerequest;

import static com.dario.ast.core.domain.RequestType.PRE_REQUEST;
import static com.dario.ast.core.domain.RunParams.defaultRunParams;
import static com.dario.ast.util.EventUtil.preRequestCreated;
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
public class CreatePreRequestButton extends Button {

  private final RequestService requestService;
  private final AppState appState;

  public CreatePreRequestButton(RequestService requestService, AppState appState) {
    this.requestService = requestService;
    this.appState = appState;

    addClassName("create-button");
    setWidthFull();
    setText("+");

    addClickListener(event -> createPreRequest());
  }

  private void createPreRequest() {
    var currentUserId = appState.getCurrentUser().getId();
    var configParams = defaultConfigParams(currentUserId);
    var runParams = defaultRunParams();
    var preRequest = new Request(configParams, runParams, null);

    try {
      var preRequestId = requestService.create(preRequest).getConfigParams().getRequestId();
      preRequestCreated(preRequestId);
    } catch (Exception ex) {
      log.error("Error creating Pre-request", ex);
      ErrorNotification.show("Error creating Pre-request");
      throw ex;
    }
  }

  private static ConfigParams defaultConfigParams(Long userId) {
    return ConfigParams.builder()
        .requestName("New Pre-request")
        .userId(userId)
        .uri("")
        .method(GET)
        .requestBody("")
        .requestType(PRE_REQUEST)
        .active(true)
        .build();
  }

}