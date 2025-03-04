package com.dario.ast.core.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared state object to hold config and run params
 */
@Data
@NoArgsConstructor
public class StressTestConfig {

  private ConfigParams configParams;
  private RunParams runParams;
}
