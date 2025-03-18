package com.dario.ast.util;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CopyUtil {

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .configure(FAIL_ON_EMPTY_BEANS, false)
      .registerModule(new JavaTimeModule());

  public static <T> T deepCopy(T object, Class<T> clazz) {
    try {
      return MAPPER.readValue(MAPPER.writeValueAsBytes(object), clazz);
    } catch (Exception e) {
      throw new RuntimeException("Deep copy failed", e);
    }
  }

}
