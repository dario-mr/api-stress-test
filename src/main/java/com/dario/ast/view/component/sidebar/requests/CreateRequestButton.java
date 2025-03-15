package com.dario.ast.view.component.sidebar.requests;

import static com.dario.ast.util.EventUtil.asrRequestCreated;
import static org.springframework.http.HttpMethod.GET;

import com.dario.ast.core.domain.ApplicationState;
import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@UIScope
@SpringComponent
public class CreateRequestButton extends Button {

  private final AstRequestService astRequestService;
  private final ApplicationState applicationState;

  @Autowired
  public CreateRequestButton(AstRequestService astRequestService, ApplicationState applicationState) {
    this.astRequestService = astRequestService;
    this.applicationState = applicationState;

    addClassName("create-button");
    setWidthFull();
    setText("+");

    addClickListener(event -> createAsrRequest());
  }

  private void createAsrRequest() {
    var currentUserId = applicationState.getCurrentUser().getId();
    var configParams = defaultConfigParams(currentUserId);
    var runParams = defaultRunParams();
    var astRequest = new AstRequest(configParams, runParams);

    try {
      var astRequestId = astRequestService.create(astRequest);
      asrRequestCreated(astRequestId);
    } catch (Exception ex) {
      log.error("Error creating new Request", ex);
      ErrorNotification.show("Error creating Request");
      throw new RuntimeException(ex);
    }
  }

  private static ConfigParams defaultConfigParams(Long userId) {
    return ConfigParams.builder()
        .requestName("New Request")
        .userId(userId)
        .uri("")
        .method(GET)
        .requestBody("")
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