package com.dario.ast.view.component.sidebar.environment;

import static com.dario.ast.util.EventUtil.environmentCreated;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.service.EnvironmentService;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UIScope
@SpringComponent
public class CreateEnvButton extends Button {

  private final EnvironmentService environmentService;
  private final AppState appState;

  public CreateEnvButton(EnvironmentService environmentService, AppState appState) {
    this.environmentService = environmentService;
    this.appState = appState;

    addClassName("create-button");
    setWidthFull();
    setText("+");

    addClickListener(event -> createEnv());
  }

  private void createEnv() {
    var currentUserId = appState.getCurrentUser().getId();
    var newEnv = defaultEnv(currentUserId);

    try {
      var envId = environmentService.create(newEnv);
      environmentCreated(envId);
    } catch (Exception ex) {
      log.error("Error creating new Environment", ex);
      ErrorNotification.show("Error creating Environment");
      throw ex;
    }
  }

  private static Environment defaultEnv(Long userId) {
    return Environment.builder()
        .id(null)
        .name("New Environment")
        .userId(userId)
        .build();
  }

}