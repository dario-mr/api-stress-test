package com.dario.ast.core.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class User {

  private Long id;
  private String email;
  private Instant createdOn;
  private boolean active;
}
