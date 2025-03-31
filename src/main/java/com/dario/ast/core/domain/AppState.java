package com.dario.ast.core.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Holds the current state of the application, including configuration parameters, run parameters, and the selected
 * environment.
 * <p>
 * This shared state is used to maintain and manage settings across different components of the application.
 */
@Getter
@Component
public class AppState {

  private ConfigParams configParams;
  private RunParams runParams;
  private Environment selectedEnvironment;
  @Setter
  private User currentUser;

  private final Sinks.Many<ConfigParams> configParamsSink = Sinks.many().replay().latest();
  private final Sinks.Many<RunParams> runParamsSink = Sinks.many().replay().latest();
  private final Sinks.Many<Environment> selectedEnvironmentSink = Sinks.many().replay().latest();

  public Flux<ConfigParams> getConfigParamsStream() {
    return configParamsSink.asFlux();
  }

  public Flux<RunParams> getRunParamsStream() {
    return runParamsSink.asFlux();
  }

  public Flux<Environment> getSelectedEnvironmentStream() {
    return selectedEnvironmentSink.asFlux();
  }

  public void setConfigParams(ConfigParams configParams) {
    this.configParams = configParams;
    configParamsSink.tryEmitNext(configParams);
  }

  public void setRunParams(RunParams runParams) {
    this.runParams = runParams;
    runParamsSink.tryEmitNext(runParams);
  }

  public void setSelectedEnvironment(Environment selectedEnvironment) {
    this.selectedEnvironment = selectedEnvironment;
    selectedEnvironmentSink.tryEmitNext(selectedEnvironment);
  }

}
