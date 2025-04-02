package com.dario.ast.view.component.environment;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.service.AstEnvironmentService;
import com.dario.ast.event.EnvironmentCreatedEvent;
import com.dario.ast.event.EnvironmentDeletedEvent;
import com.dario.ast.event.EnvironmentUpdatedEvent;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent
public class EnvironmentCombo extends ComboBox<Environment> {

  private final AstEnvironmentService environmentService;
  private final AppState appState;

  public EnvironmentCombo(AstEnvironmentService environmentService, AppState appState) {
    this.environmentService = environmentService;
    this.appState = appState;

    loadEnvironments();
    addValueChangeListener(event -> setSelectedEnvironment(event.getValue()));
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Listen for events indicating that an Environment was updated
    ComponentUtil.addListener(attachEvent.getUI(),
        EnvironmentUpdatedEvent.class,
        event -> reloadEnvironments()
    );

    // Listen for events indicating that a new Environment was created
    ComponentUtil.addListener(attachEvent.getUI(),
        EnvironmentCreatedEvent.class,
        event -> reloadEnvironments()
    );

    // Listen for events indicating that an Environment was deleted
    ComponentUtil.addListener(attachEvent.getUI(),
        EnvironmentDeletedEvent.class,
        event -> reloadEnvironments()
    );
  }

  private void loadEnvironments() {
    var currentUserId = appState.getCurrentUser().getId();
    var userEnvironments = environmentService.getByUserId(currentUserId);
    setItems(userEnvironments);

    if (!userEnvironments.isEmpty()) {
      var environment = userEnvironments.getFirst();
      setValue(environment);
      setSelectedEnvironment(environment);
    }
  }

  private void reloadEnvironments() {
    var currentEnv = getValue(); // save currently selected environment
    var userEnvironments = environmentService.getByUserId(appState.getCurrentUser().getId());
    setItems(userEnvironments);

    if (userEnvironments.isEmpty()) {
      return;
    }

    var newSelection = userEnvironments.stream()
        .filter(env -> currentEnv != null && env.getId().equals(currentEnv.getId()))
        .findFirst()
        .orElse(userEnvironments.getFirst());

    setValue(newSelection);
    setSelectedEnvironment(newSelection);
  }

  private void setSelectedEnvironment(Environment environment) {
    if (environment != null) {
      appState.setSelectedEnvironment(environment);
    }
  }

}
