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
@EqualsAndHashCode(of = "id")
public class Environment {

  private Long id;
  private String name;
  private Long userId;

  @Override
  public String toString() {
    return name;
  }
}
