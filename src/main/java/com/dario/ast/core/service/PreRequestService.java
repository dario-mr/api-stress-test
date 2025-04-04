package com.dario.ast.core.service;

import static com.dario.ast.util.EnvironmentUtil.applyEnvironmentVariables;
import static java.util.stream.Collectors.toMap;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.proxy.ApiProxy;
import com.dario.ast.proxy.ApiResponse;
import com.dario.ast.view.component.common.notification.WarnNotification;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreRequestService {

  private final AppState appState;
  private final ApiProxy apiProxy;
  private final AstEnvironmentService astEnvironmentService;

  public void updatePreRequestInAppState(ConfigParams updatedPreRequestParams) {
    var currentPreRequestsParams = new ArrayList<>(appState.getPreRequestsParams());

    currentPreRequestsParams.replaceAll(preRequestParams ->
        Objects.equals(preRequestParams.getRequestId(), updatedPreRequestParams.getRequestId())
            ? updatedPreRequestParams
            : preRequestParams
    );

    appState.setPreRequestsParams(currentPreRequestsParams);
  }

  public void runAndApplyPreRequests() {
    var selectedEnvironment = appState.getSelectedEnvironment();
    var activePreRequests = getActivePreRequestsParams();
    var envPreRequests = applyEnvToPreRequests(selectedEnvironment, activePreRequests);
    var preRequestsResults = runPreRequests(envPreRequests);

    applyResultsToEnv(preRequestsResults, selectedEnvironment);
  }

  private List<ConfigParams> getActivePreRequestsParams() {
    return appState.getPreRequestsParams().stream()
        .filter(ConfigParams::isActive)
        .toList();
  }

  private List<ConfigParams> applyEnvToPreRequests(
      Environment selectedEnvironment, List<ConfigParams> activePreRequests) {
    return activePreRequests.stream()
        .map(preRequestParams -> applyEnvironmentVariables(preRequestParams, selectedEnvironment))
        .toList();
  }

  private Map<String, ApiResponse> runPreRequests(List<ConfigParams> preRequestsParams) {
    return preRequestsParams.stream()
        .map(preRequestParams -> {
          var response = apiProxy.makeRequest(preRequestParams.toApiRequest());
          if (response.statusCode().isError() || response.responseBody() == null) {
            WarnNotification.show("Pre-request [%s] failed".formatted(preRequestParams.getRequestName()));
            return null;
          }
          return new SimpleEntry<>(preRequestParams.getRequestName(), response);
        })
        .filter(Objects::nonNull)
        .collect(toMap(Entry::getKey, Entry::getValue));
  }

  private void applyResultsToEnv(Map<String, ApiResponse> preRequestsResults, Environment environment) {
    var environmentVariables = environment.getVariables();

    preRequestsResults.keySet().stream()
        .filter(environmentVariables::containsKey)
        .forEach(requestName -> {
          var envVariable = environmentVariables.get(requestName);
          envVariable.setValue(preRequestsResults.get(requestName).responseBody());
        });

    astEnvironmentService.update(environment);
    astEnvironmentService.updateEnvironmentInAppState(environment);
  }

}
