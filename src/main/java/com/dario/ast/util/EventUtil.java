package com.dario.ast.util;

import static com.vaadin.flow.component.ComponentUtil.fireEvent;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.event.ApplyEnvironmentEvent;
import com.dario.ast.event.ApplyPreRequestEvent;
import com.dario.ast.event.AstRequestCreatedEvent;
import com.dario.ast.event.ConfigEntriesUpdatedEvent;
import com.dario.ast.event.EnvironmentCreatedEvent;
import com.dario.ast.event.EnvironmentDeletedEvent;
import com.dario.ast.event.EnvironmentEntriesUpdatedEvent;
import com.dario.ast.event.EnvironmentUpdatedEvent;
import com.dario.ast.event.FocusEnvNameEvent;
import com.dario.ast.event.FocusPreRequestNameEvent;
import com.dario.ast.event.FocusRequestNameEvent;
import com.dario.ast.event.PreRequestCreatedEvent;
import com.dario.ast.event.PreRequestUpdatedEvent;
import com.vaadin.flow.component.UI;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EventUtil {

  public static void configEntriesUpdated() {
    fireEvent(UI.getCurrent(), new ConfigEntriesUpdatedEvent());
  }

  public static void astRequestCreated(Long astRequestId) {
    fireEvent(UI.getCurrent(), new AstRequestCreatedEvent(astRequestId));
  }

  public static void preRequestCreated(Long astRequestId) {
    fireEvent(UI.getCurrent(), new PreRequestCreatedEvent(astRequestId));
  }

  public static void preRequestUpdated(AstRequest preRequest) {
    fireEvent(UI.getCurrent(), new PreRequestUpdatedEvent(preRequest));
  }

  public static void applyPreRequest(AstRequest preRequest) {
    fireEvent(UI.getCurrent(), new ApplyPreRequestEvent(preRequest));
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

  public static void focusPreRequestName() {
    fireEvent(UI.getCurrent(), new FocusPreRequestNameEvent());
  }
}
