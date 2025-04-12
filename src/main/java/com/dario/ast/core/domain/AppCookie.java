package com.dario.ast.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppCookie {

  USER_ID("userId");

  private final String name;
}
