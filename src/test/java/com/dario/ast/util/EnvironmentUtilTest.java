package com.dario.ast.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.EnvVariable;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.domain.RequestHeader;
import com.dario.ast.core.domain.RequestQueryParam;
import com.dario.ast.core.domain.RequestUriVariable;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EnvironmentUtilTest {

  @Test
  void applyEnvironmentVariables_whenEnvironmentIsNull_shouldReturnSameConfigParams() {
    // given
    var original = ConfigParams.builder()
        .uri("https://example.com")
        .requestBody("body")
        .headers(Map.of())
        .uriVariables(Map.of())
        .queryParams(Map.of())
        .build();

    // when
    var result = EnvironmentUtil.applyEnvironmentVariables(original, null);

    // then
    assertThat(result).isSameAs(original);
  }

  @Test
  void applyEnvironmentVariables_whenVariablesAreEmpty_shouldReturnSameConfigParams() {
    // given
    var original = ConfigParams.builder()
        .uri("https://example.com")
        .requestBody("body")
        .headers(Map.of())
        .uriVariables(Map.of())
        .queryParams(Map.of())
        .build();

    var environment = Environment.builder()
        .variables(Map.of())
        .build();

    // when
    var result = EnvironmentUtil.applyEnvironmentVariables(original, environment);

    // then
    assertThat(result).isSameAs(original);
  }

  @Test
  void applyEnvironmentVariables_shouldReplaceVariablesInUri() {
    // given
    var original = ConfigParams.builder()
        .uri("https://example.com/{{VAR1}}/path")
        .build();

    var environment = Environment.builder()
        .variables(Map.of("VAR1", new EnvVariable("replaced", null)))
        .build();

    // when
    var result = EnvironmentUtil.applyEnvironmentVariables(original, environment);

    // then
    assertThat(result.getUri()).isEqualTo("https://example.com/replaced/path");
  }

  @Test
  void applyEnvironmentVariables_shouldReplaceVariablesInHeaders() {
    // given
    var original = ConfigParams.builder()
        .headers(Map.of("Authorization", new RequestHeader("Bearer {{TOKEN}}", null)))
        .build();

    var environment = Environment.builder()
        .variables(Map.of("TOKEN", new EnvVariable("abc123", null)))
        .build();

    // when
    var result = EnvironmentUtil.applyEnvironmentVariables(original, environment);

    // then
    assertThat(result.getHeaders()).containsEntry("Authorization", new RequestHeader("Bearer abc123", null));
  }

  @Test
  void applyEnvironmentVariables_shouldReplaceVariablesInUriVariables() {
    // given
    var original = ConfigParams.builder()
        .uriVariables(Map.of("id", new RequestUriVariable("{{USER_ID}}", null)))
        .build();

    var environment = Environment.builder()
        .variables(Map.of("USER_ID", new EnvVariable("42", null)))
        .build();

    // when
    var result = EnvironmentUtil.applyEnvironmentVariables(original, environment);

    // then
    assertThat(result.getUriVariables()).containsEntry("id", new RequestUriVariable("42", null));
  }

  @Test
  void applyEnvironmentVariables_shouldReplaceVariablesInQueryParams() {
    // given
    var original = ConfigParams.builder()
        .queryParams(Map.of("q", new RequestQueryParam("search {{TERM}}", null)))
        .build();

    var environment = Environment.builder()
        .variables(Map.of("TERM", new EnvVariable("test", null)))
        .build();

    // when
    var result = EnvironmentUtil.applyEnvironmentVariables(original, environment);

    // then
    assertThat(result.getQueryParams()).containsEntry("q", new RequestQueryParam("search test", null));
  }

  @Test
  void applyEnvironmentVariables_shouldReplaceVariablesInRequestBody() {
    // given
    var original = ConfigParams.builder()
        .requestBody("{ \"user\": \"{{USERNAME}}\" }")
        .build();

    var environment = Environment.builder()
        .variables(Map.of("USERNAME", new EnvVariable("john_doe", null)))
        .build();

    // when
    var result = EnvironmentUtil.applyEnvironmentVariables(original, environment);

    // then
    assertThat(result.getRequestBody()).isEqualTo("{ \"user\": \"john_doe\" }");
  }

}