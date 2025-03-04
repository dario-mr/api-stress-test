package com.dario.ast.core.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Shared state object to hold config and run params
 */
@Getter
@Setter
@Component
public class StressTestConfig {

  private ConfigParams configParams;
  private RunParams runParams;
}
