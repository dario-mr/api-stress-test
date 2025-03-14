package com.dario.ast.view.component.sidebar.environments;

import static com.dario.ast.util.EventUtil.environmentCreated;

import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.service.AstEnvironmentService;
import com.dario.ast.core.service.AstUserService;
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
  private final AstUserService astUserService;

  @Autowired
  public CreateEnvButton(AstEnvironmentService astEnvironmentService, AstUserService astUserService) {
    this.astEnvironmentService = astEnvironmentService;
    this.astUserService = astUserService;

    addClassName("create-button");
    setWidthFull();
    setText("+");

    addClickListener(event -> createEnv());
  }

  private void createEnv() {
    var currentUser = astUserService.getCurrentUser();
    var newEnv = new Environment(null, "New Environment", currentUser.getId());

    try {
      var envId = astEnvironmentService.create(newEnv);
      environmentCreated(envId);
    } catch (Exception ex) {
      log.error("Error creating new Environment", ex);
      ErrorNotification.show("Error creating Environment");
      throw new RuntimeException(ex);
    }
  }

}