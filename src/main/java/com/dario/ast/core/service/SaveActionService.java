package com.dario.ast.core.service;

import static com.dario.ast.util.EventUtil.asrRequestUpdated;
import static com.dario.ast.util.EventUtil.environmentUpdated;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveActionService {

  private final AstRequestService astRequestService;
  private final AstEnvironmentService astEnvironmentService;

  public void saveParams(ConfigParams configParams, RunParams runParams) {
    var asrRequest = new AstRequest(configParams, runParams);

    try {
      astRequestService.update(asrRequest);
    } catch (Exception ex) {
      log.error("Error saving parameters", ex);
      ErrorNotification.show("Error saving parameters");
      throw new RuntimeException(ex);
    }

    asrRequestUpdated(asrRequest);
  }

  public void saveEnvironment(Environment environment) {
    try {
      astEnvironmentService.update(environment);
    } catch (Exception ex) {
      log.error("Error saving environment", ex);
      ErrorNotification.show("Error saving environment");
      throw new RuntimeException(ex);
    }

    environmentUpdated(environment);
  }
}
