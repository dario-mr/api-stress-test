package com.dario.ast.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "requestId")
public final class RunParams {

  private Long requestId;
  private int numRequests;
  private int threadPoolSize;
  private boolean stopOnError;
}
