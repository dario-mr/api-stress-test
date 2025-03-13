package com.dario.ast.view.component.environment;

import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.domain.ApplicationState;
import com.dario.ast.core.service.AstEnvironmentService;
import com.dario.ast.core.service.UserSessionService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent
public class EnvironmentCombo extends ComboBox<Environment> {

  private final ApplicationState applicationState;

  public EnvironmentCombo(
      UserSessionService userSessionService,
      AstEnvironmentService environmentService,
      ApplicationState applicationState
  ) {
    this.applicationState = applicationState;

    var currentGoogleUser = userSessionService.getUser();
    var environments = environmentService.getByEmail(currentGoogleUser.email());

    setItems(environments);
    if (!environments.isEmpty()) {
      var environment = environments.getFirst();
      setValue(environment);
      applicationState.setEnvironment(environment);
    }

    addValueChangeListener(event -> selectValue(event.getValue()));
  }

  private void selectValue(Environment environment) {
    applicationState.setEnvironment(environment);
  }

}
