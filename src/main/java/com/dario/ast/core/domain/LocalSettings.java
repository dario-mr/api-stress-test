package com.dario.ast.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LocalSettings {

  THEME("theme", "dark");

  private final String name;
  private final String defaultValue;
}
