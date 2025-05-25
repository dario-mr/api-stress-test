package com.dario.ast.util;

import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtil {

  public static long average(List<Long> elements) {
    return Math.round(
        elements.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0)
    );
  }

}
