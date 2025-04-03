package com.dario.ast.core.domain;

import java.util.Arrays;

public enum RequestType {
  REQUEST,
  PRE_REQUEST,
  UNKNOWN;

  public static RequestType fromString(String value) {
    if (value == null) {
      return null;
    }

    return Arrays.stream(values())
        .filter(requestType -> requestType.toString().equals(value.toUpperCase()))
        .findFirst()
        .orElse(UNKNOWN);
  }

}
