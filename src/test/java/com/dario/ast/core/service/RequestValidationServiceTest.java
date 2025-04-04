package com.dario.ast.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.springframework.http.HttpMethod.GET;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.ValidationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;

class RequestValidationServiceTest {

  private final RequestValidationService requestValidationService = new RequestValidationService();

  private static Stream<Arguments> getValidateParams() {
    return Stream.of(
        of(buildConfigParams("name", "uri", GET), success()),
        of(buildConfigParams(null, "uri", GET), fail("Please provide a valid Request name")),
        of(buildConfigParams("name", null, GET), fail("Please provide a valid URL")),
        of(buildConfigParams("name", "uri", null), fail("Please provide a valid HTTP method"))
    );
  }

  @ParameterizedTest
  @MethodSource("getValidateParams")
  void validate(ConfigParams params, ValidationResult expectedResult) {
    assertThat(requestValidationService.validate(params)).isEqualTo(expectedResult);
  }

  private static ConfigParams buildConfigParams(String requestName, String uri, HttpMethod httpMethod) {
    return ConfigParams.builder()
        .requestName(requestName)
        .uri(uri)
        .method(httpMethod)
        .build();
  }

  private static ValidationResult success() {
    return new ValidationResult(true, null);
  }

  private static ValidationResult fail(String message) {
    return new ValidationResult(false, message);
  }

}
