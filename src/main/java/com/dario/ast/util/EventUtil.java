package com.dario.ast.util;

import static com.vaadin.flow.component.ComponentUtil.fireEvent;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.domain.Folder;
import com.dario.ast.core.domain.Request;
import com.dario.ast.event.apply.ApplyEnvironmentEvent;
import com.dario.ast.event.apply.ApplyPreRequestEvent;
import com.dario.ast.event.created.EnvironmentCreatedEvent;
import com.dario.ast.event.created.PreRequestCreatedEvent;
import com.dario.ast.event.created.RequestCreatedEvent;
import com.dario.ast.event.focus.FocusEnvNameEvent;
import com.dario.ast.event.focus.FocusPreRequestNameEvent;
import com.dario.ast.event.focus.FocusRequestNameEvent;
import com.dario.ast.event.folder.FocusFolderNameEvent;
import com.dario.ast.event.folder.FolderCreatedEvent;
import com.dario.ast.event.folder.FolderSelectedEvent;
import com.dario.ast.event.folder.FolderUpdatedEvent;
import com.dario.ast.event.stresstest.ApiRequestCompletedEvent;
import com.dario.ast.event.stresstest.StressTestStartedEvent;
import com.dario.ast.event.updated.ConfigEntriesUpdatedEvent;
import com.dario.ast.event.updated.EnvironmentEntriesUpdatedEvent;
import com.dario.ast.event.updated.PreRequestConfigEntriesUpdatedEvent;
import com.dario.ast.proxy.api.dto.ApiResponse;
import com.vaadin.flow.component.UI;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EventUtil {

  public static void configEntriesUpdated() {
    fireEvent(UI.getCurrent(), new ConfigEntriesUpdatedEvent());
  }

  public static void requestCreated(Request request) {
    fireEvent(UI.getCurrent(), new RequestCreatedEvent(request));
  }

  public static void preRequestCreated(Long requestId) {
    fireEvent(UI.getCurrent(), new PreRequestCreatedEvent(requestId));
  }

  public static void applyPreRequest(ConfigParams preRequestConfigParams) {
    fireEvent(UI.getCurrent(), new ApplyPreRequestEvent(preRequestConfigParams));
  }

  public static void preRequestConfigEntriesUpdated() {
    fireEvent(UI.getCurrent(), new PreRequestConfigEntriesUpdatedEvent());
  }

  public static void applyEnvironment(Environment environment) {
    fireEvent(UI.getCurrent(), new ApplyEnvironmentEvent(environment));
  }

  public static void environmentCreated(Long envId) {
    fireEvent(UI.getCurrent(), new EnvironmentCreatedEvent(envId));
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

  public static void folderSelected(Folder folder) {
    fireEvent(UI.getCurrent(), new FolderSelectedEvent(folder));
  }

  public static void folderUpdated(Folder folder) {
    fireEvent(UI.getCurrent(), new FolderUpdatedEvent(folder));
  }

  public static void folderCreated(Folder folder) {
    fireEvent(UI.getCurrent(), new FolderCreatedEvent(folder));
  }

  public static void focusFolderName() {
    fireEvent(UI.getCurrent(), new FocusFolderNameEvent());
  }

  public static void apiRequestCompleted(ApiResponse apiResponse) {
    fireEvent(UI.getCurrent(), new ApiRequestCompletedEvent(apiResponse));
  }

  public static void stressTestStarted() {
    fireEvent(UI.getCurrent(), new StressTestStartedEvent());
  }

}
