package com.dario.ast.util;

import static com.vaadin.flow.component.ComponentUtil.fireEvent;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.event.ApplyConfigParamsEvent;
import com.dario.ast.event.ApplyEnvironmentEvent;
import com.dario.ast.event.ApplyRunParamsEvent;
import com.dario.ast.event.AsrRequestCreatedEvent;
import com.dario.ast.event.AsrRequestUpdatedEvent;
import com.dario.ast.event.ConfigEntriesUpdatedEvent;
import com.dario.ast.event.EnvironmentCreatedEvent;
import com.dario.ast.event.EnvironmentDeletedEvent;
import com.dario.ast.event.EnvironmentEntriesUpdatedEvent;
import com.dario.ast.event.EnvironmentUpdatedEvent;
import com.dario.ast.event.FocusEnvNameEvent;
import com.dario.ast.event.FocusRequestNameEvent;
import com.vaadin.flow.component.UI;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EventUtil {

  public static void applyConfigParams(ConfigParams configParams) {
    fireEvent(UI.getCurrent(), new ApplyConfigParamsEvent(configParams));
  }

  public static void applyRunParams(RunParams runParams) {
    fireEvent(UI.getCurrent(), new ApplyRunParamsEvent(runParams));
  }

  public static void configEntriesUpdated() {
    fireEvent(UI.getCurrent(), new ConfigEntriesUpdatedEvent());
  }

  public static void asrRequestUpdated(AstRequest astRequest) {
    fireEvent(UI.getCurrent(), new AsrRequestUpdatedEvent(astRequest));
  }

  public static void asrRequestCreated(Long asrRequestId) {
    fireEvent(UI.getCurrent(), new AsrRequestCreatedEvent(asrRequestId));
  }

  public static void applyEnvironment(Environment environment) {
    fireEvent(UI.getCurrent(), new ApplyEnvironmentEvent(environment));
  }

  public static void environmentUpdated(Environment environment) {
    fireEvent(UI.getCurrent(), new EnvironmentUpdatedEvent(environment));
  }

  public static void environmentCreated(Long envId) {
    fireEvent(UI.getCurrent(), new EnvironmentCreatedEvent(envId));
  }

  public static void environmentDeleted(Long envId) {
    fireEvent(UI.getCurrent(), new EnvironmentDeletedEvent(envId));
  }

  public static void environmentEntriesUpdated() {
    fireEvent(UI.getCurrent(), new EnvironmentEntriesUpdatedEvent());
  }

  public static void focusEnvName() {
    fireEvent(UI.getCurrent(), new FocusEnvNameEvent());
  }

  public static void focusRequestName() {
    fireEvent(UI.getCurrent(), new FocusRequestNameEvent());
  }
}
