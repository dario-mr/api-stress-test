package com.dario.ast.core.service;

import static org.springframework.util.StringUtils.hasText;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.ValidationResult;
import org.springframework.stereotype.Service;

@Service
public class RequestValidationService {

  public ValidationResult validate(ConfigParams configParams) {
    if (!hasText(configParams.getRequestName())) {
      return fail("Please provide a valid Request name");
    }
    if (!hasText(configParams.getUri())) {
      return fail("Please provide a valid URL");
    }
    if (configParams.getMethod() == null || !hasText(configParams.getMethod().toString())) {
      return fail("Please provide a valid HTTP method");
    }

    return success();
  }

  private static ValidationResult success() {
    return new ValidationResult(true, null);
  }

  private static ValidationResult fail(String message) {
    return new ValidationResult(false, message);
  }

}
