package com.dario.ast.core.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Holds the current state of the application, including configuration parameters, run parameters, and the selected
 * environment.
 * <p>
 * This shared state is used to maintain and manage settings across different components of the application.
 */
@Getter
@Setter
@Component
public class ApplicationState {
  // TODO move current user here? it requires to be populated at app start

  private ConfigParams configParams;
  private RunParams runParams;
  private Environment environment;
}
