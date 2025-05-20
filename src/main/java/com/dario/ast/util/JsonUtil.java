package com.dario.ast.util;

import static org.springframework.util.StringUtils.hasText;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtil {

  private static final ObjectMapper JSON_FORMATTER = new ObjectMapper()
      .enable(SerializationFeature.INDENT_OUTPUT);

  public static String prettifyJson(String json) {
    // quick sanity checks
    if (!hasText(json)) {
      return "";
    }

    var trimmed = json.trim();
    if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))
        && !(trimmed.startsWith("[") && trimmed.endsWith("]"))) {
      return json;
    }

    try {
      var jsonObject = JSON_FORMATTER.readValue(json, Object.class);
      return JSON_FORMATTER.writeValueAsString(jsonObject);
    } catch (Exception e) {
      return json;
    }
  }

}
