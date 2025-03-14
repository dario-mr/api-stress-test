package com.dario.ast.view.component.sidebar.environments;

import static com.dario.ast.util.EventUtil.environmentCreated;

import com.dario.ast.core.domain.ApplicationState;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.service.AstEnvironmentService;
import com.dario.ast.view.component.notification.ErrorNotification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@UIScope
@SpringComponent
public class CreateEnvButton extends Button {

  private final AstEnvironmentService astEnvironmentService;
  private final ApplicationState applicationState;

  @Autowired
  public CreateEnvButton(AstEnvironmentService astEnvironmentService, ApplicationState applicationState) {
    this.astEnvironmentService = astEnvironmentService;
    this.applicationState = applicationState;

    addClassName("create-button");
    setWidthFull();
    setText("+");

    addClickListener(event -> createEnv());
  }

  private void createEnv() {
    var currentUserId = applicationState.getCurrentUser().getId();
    var newEnv = defaultEnv(currentUserId);

    try {
      var envId = astEnvironmentService.create(newEnv);
      environmentCreated(envId);
    } catch (Exception ex) {
      log.error("Error creating new Environment", ex);
      ErrorNotification.show("Error creating Environment");
      throw new RuntimeException(ex);
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