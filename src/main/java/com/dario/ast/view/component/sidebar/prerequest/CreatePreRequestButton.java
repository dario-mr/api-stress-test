package com.dario.ast.view.component.sidebar.prerequest;

import static com.dario.ast.core.domain.RequestType.PRE_REQUEST;
import static com.dario.ast.util.EventUtil.astRequestCreated;
import static com.dario.ast.util.EventUtil.preRequestCreated;
import static org.springframework.http.HttpMethod.GET;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UIScope
@SpringComponent
public class CreatePreRequestButton extends Button {

  private final AstRequestService astRequestService;
  private final AppState appState;

  public CreatePreRequestButton(AstRequestService astRequestService, AppState appState) {
    this.astRequestService = astRequestService;
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
    var preRequest = new AstRequest(configParams, runParams);

    try {
      var preRequestId = astRequestService.create(preRequest);
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

  private static RunParams defaultRunParams() {
    return RunParams.builder()
        .numRequests(1)
        .threadPoolSize(1)
        .stopOnError(true)
        .build();
  }
}