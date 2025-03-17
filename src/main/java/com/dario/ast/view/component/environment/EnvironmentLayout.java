package com.dario.ast.view.component.environment;

import static com.dario.ast.core.domain.EnvVariable.defaultEnvVariable;
import static com.dario.ast.util.MapUtil.removeGenericEmptyEntries;
import static org.springframework.util.StringUtils.hasText;

import com.dario.ast.core.domain.EnvVariable;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.service.SaveActionService;
import com.dario.ast.event.ApplyEnvironmentEvent;
import com.dario.ast.event.EnvironmentEntriesUpdatedEvent;
import com.dario.ast.event.FocusEnvNameEvent;
import com.dario.ast.util.EventUtil;
import com.dario.ast.view.component.common.entries.EntriesSection;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

@UIScope
@SpringComponent
@CssImport(value = "./styles/env-layout.css")
public class EnvironmentLayout extends VerticalLayout {

  private final SaveActionService saveActionService;

  private final TextField nameText = new TextField();
  private final EntriesSection<EnvVariable> variablesSection = new EntriesSection<>(
      EventUtil::environmentEntriesUpdated, EnvVariable::defaultEnvVariable);

  private Environment selectedEnvironment;

  @Autowired
  public EnvironmentLayout(SaveActionService saveActionService) {
    this.saveActionService = saveActionService;

    addClassNames("card-layout", "env-layout");
    setWidthFull();

    // name
    nameText.setPlaceholder("Enter the request name");
    nameText.setWidthFull();
    nameText.setMaxWidth("20em");
    nameText.setMinWidth("0");

    // add listeners
    addListeners();

    add(
        new H4("Environment"),
        nameText,
        new H4("Variables"),
        variablesSection
    );
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Listen for events that should trigger applying the env params in the UI
    ComponentUtil.addListener(attachEvent.getUI(),
        ApplyEnvironmentEvent.class,
        event -> applyEnv(event.getEnvironment())
    );

    // Listen for events indicating that the env name field should be focused
    ComponentUtil.addListener(attachEvent.getUI(),
        FocusEnvNameEvent.class,
        event -> nameText.focus()
    );

    // Listen for events indicating that env entries (EntriesSection class) were updated
    ComponentUtil.addListener(attachEvent.getUI(),
        EnvironmentEntriesUpdatedEvent.class,
        event -> saveChanges()
    );
  }

  private void applyEnv(Environment environment) {
    selectedEnvironment = environment;

    nameText.setValue(environment.getName());

    variablesSection.clearEntries();
    variablesSection.addEntries(environment.getVariables());
    variablesSection.addEntry("", defaultEnvVariable());
  }

  private void addListeners() {
    // name
    nameText.addBlurListener(event -> {
      var newValue = nameText.getValue();
      if (hasText(newValue)) {
        saveChanges();
      }
    });
  }

  private void saveChanges() {
    selectedEnvironment = getEnvParams();
    saveActionService.saveEnvironment(selectedEnvironment);
  }

  private Environment getEnvParams() {
    var name = nameText.getValue();
    var variables = removeGenericEmptyEntries(variablesSection.getEntries());

    return Environment.builder()
        .id(selectedEnvironment.getId())
        .userId(selectedEnvironment.getUserId())
        .name(name)
        .variables(variables)
        .build();
  }


}
