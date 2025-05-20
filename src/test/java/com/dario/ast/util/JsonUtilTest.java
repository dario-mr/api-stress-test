package com.dario.ast.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JsonUtilTest {

  @Test
  void prettifyJson_whenNullOrEmpty_returnEmptyString() {
    assertThat(JsonUtil.prettifyJson(null)).isEmpty();
    assertThat(JsonUtil.prettifyJson("")).isEmpty();
    assertThat(JsonUtil.prettifyJson("   ")).isEmpty();
  }

  @Test
  void prettifyJson_whenNonJsonInput_returnOriginal() {
    String input = "not a json";
    assertThat(JsonUtil.prettifyJson(input)).isEqualTo(input);
  }

  @Test
  void prettifyJson_whenStringDoesNotStartAndEndWithJsonBrackets_returnOriginal() {
    String input = "foo {\"a\":1}";
    assertThat(JsonUtil.prettifyJson(input)).isEqualTo(input);
  }

  @Test
  void prettifyJson_whenInvalidJson_returnOriginal() {
    String input = "{invalid json}";
    assertThat(JsonUtil.prettifyJson(input)).isEqualTo(input);
  }


  @Test
  void prettifyJson_whenValidJsonObject_returnPrettyPrinted() {
    String input = "{\"a\":1,\"b\":2}";
    String result = JsonUtil.prettifyJson(input);
    assertThat(result).isEqualTo("""
        {
          "a" : 1,
          "b" : 2
        }""");
  }

  @Test
  void prettifyJson_whenValidJsonArray_returnPrettyPrinted() {
    String input = "[{\"a\":1},{\"b\":2}]";
    String result = JsonUtil.prettifyJson(input);
    assertThat(result).isEqualTo("""
        [ {
          "a" : 1
        }, {
          "b" : 2
        } ]""");
  }

  @Test
  void prettifyJson_whenJsonWithWhitespace_returnPrettyPrinted() {
    String input = "   {\"foo\": \"bar\"}   ";
    String result = JsonUtil.prettifyJson(input);
    assertThat(result).isEqualTo("""
        {
          "foo" : "bar"
        }""");
  }

}