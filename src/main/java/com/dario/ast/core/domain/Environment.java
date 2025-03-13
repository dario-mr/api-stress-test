package com.dario.ast.core.domain;

public record Environment(
    String name
) {

  @Override
  public String toString() {
    return name;
  }
}
