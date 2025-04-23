package com.dario.ast.view.component.environment;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.service.EnvironmentService;
import com.dario.ast.view.component.common.reactive.ReactiveBinderSupport;
import com.dario.ast.view.component.common.reactive.ReactiveSubscription;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.List;
import reactor.core.publisher.Flux;

@UIScope
@SpringComponent
public class EnvironmentCombo extends ComboBox<Environment> implements ReactiveBinderSupport {

  private final EnvironmentService environmentService;
  private final AppState appState;

  private boolean isReloadingEnvs = false;

  public EnvironmentCombo(EnvironmentService environmentService, AppState appState) {
    this.environmentService = environmentService;
    this.appState = appState;

    loadEnvironments();
    addValueChangeListener(event -> {
      if (!isReloadingEnvs) {
        setSelectedEnvironment(event.getValue());
      }
    });
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    getUI().ifPresent(ui -> bindReactiveSubscriptions(this, ui));
  }

  @ReactiveSubscription
  public Flux<List<Environment>> onEnvironmentsChanged() {
    return appState.getEnvironmentsStream();
  }

  public void handle(List<Environment> environments) {
    reloadEnvironments(environments);
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

  private void reloadEnvironments(List<Environment> environments) {
    isReloadingEnvs = true;

    var currentEnv = getValue(); // save currently selected environment
    setItems(environments);

    if (environments.isEmpty()) {
      isReloadingEnvs = false;
      return;
    }

    var newSelection = environments.stream()
        .filter(env -> currentEnv != null && env.getId().equals(currentEnv.getId()))
        .findFirst()
        .orElse(environments.getFirst());

    setValue(newSelection);

    isReloadingEnvs = false;
  }

  private void setSelectedEnvironment(Environment environment) {
    if (environment != null) {
      appState.setSelectedEnvironment(environment);
    }
  }

}
