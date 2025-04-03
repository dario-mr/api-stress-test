package com.dario.ast.core.domain;

import static com.dario.ast.util.CopyUtil.deepCopy;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Holds the current state of the application, including configuration parameters, run parameters, and the selected
 * environment.
 * <p>
 * This shared state is used to maintain and manage settings across different components of the application.
 */
@Component
public class AppState {

  private ConfigParams configParams; // config params of the currently selected request
  private RunParams runParams; // run params of the currently selected request
  private Environment selectedEnvironment; // currently selected environment
  private User currentUser; // current user
  private List<ConfigParams> preRequestsParams; // pre-request params of the current user
  private List<Environment> environments; // environments of the current user

  private final Sinks.Many<ConfigParams> configParamsSink = Sinks.many().replay().latest();
  private final Sinks.Many<RunParams> runParamsSink = Sinks.many().replay().latest();
  private final Sinks.Many<Environment> selectedEnvironmentSink = Sinks.many().replay().latest();
  private final Sinks.Many<List<ConfigParams>> preRequestsParamsSink = Sinks.many().replay().latest();
  private final Sinks.Many<List<Environment>> environmentsSink = Sinks.many().replay().latest();

  public ConfigParams getConfigParams() {
    return deepCopy(configParams, ConfigParams.class);
  }

  public void setConfigParams(ConfigParams configParams) {
    this.configParams = deepCopy(configParams, ConfigParams.class);
    configParamsSink.tryEmitNext(configParams);
  }

  public RunParams getRunParams() {
    return deepCopy(runParams, RunParams.class);
  }

  public void setRunParams(RunParams runParams) {
    this.runParams = runParams;
    runParamsSink.tryEmitNext(runParams);
  }

  public Environment getSelectedEnvironment() {
    return deepCopy(selectedEnvironment, Environment.class);
  }

  public void setSelectedEnvironment(Environment selectedEnvironment) {
    this.selectedEnvironment = selectedEnvironment;
    selectedEnvironmentSink.tryEmitNext(selectedEnvironment);
  }

  public User getCurrentUser() {
    return deepCopy(currentUser, User.class);
  }

  public void setCurrentUser(User currentUser) {
    this.currentUser = deepCopy(currentUser, User.class);
  }

  public List<ConfigParams> getPreRequestsParams() {
    return deepCopy(preRequestsParams, new TypeReference<>() {
    });
  }

  public void setPreRequestsParams(List<ConfigParams> preRequestsParams) {
    this.preRequestsParams = deepCopy(preRequestsParams, new TypeReference<>() {
    });
    preRequestsParamsSink.tryEmitNext(preRequestsParams);
  }

  public void addPreRequestParams(ConfigParams preRequestParams) {
    preRequestsParams.add(deepCopy(preRequestParams, ConfigParams.class));
    preRequestsParamsSink.tryEmitNext(deepCopy(preRequestsParams, new TypeReference<>() {
    }));
  }

  public void removePreRequestParams(ConfigParams preRequestParams) {
    preRequestsParams.remove(preRequestParams);
    preRequestsParamsSink.tryEmitNext(deepCopy(preRequestsParams, new TypeReference<>() {
    }));
  }

  public List<Environment> getEnvironments() {
    return deepCopy(environments, new TypeReference<>() {
    });
  }

  public void setEnvironments(List<Environment> environments) {
    this.environments = deepCopy(environments, new TypeReference<>() {
    });
    environmentsSink.tryEmitNext(environments);
  }

  public void addEnvironment(Environment environment) {
    environments.add(deepCopy(environment, Environment.class));
    environmentsSink.tryEmitNext(deepCopy(environments, new TypeReference<>() {
    }));
  }

  public void removeEnvironment(Environment environment) {
    environments.remove(environment);
    environmentsSink.tryEmitNext(deepCopy(environments, new TypeReference<>() {
    }));
  }

  public Flux<ConfigParams> getConfigParamsStream() {
    return configParamsSink.asFlux();
  }

  public Flux<RunParams> getRunParamsStream() {
    return runParamsSink.asFlux();
  }

  public Flux<Environment> getSelectedEnvironmentStream() {
    return selectedEnvironmentSink.asFlux();
  }

  public Flux<List<ConfigParams>> getPreRequestsParamsStream() {
    return preRequestsParamsSink.asFlux();
  }

  public Flux<List<Environment>> getEnvironmentsStream() {
    return environmentsSink.asFlux();
  }

}
