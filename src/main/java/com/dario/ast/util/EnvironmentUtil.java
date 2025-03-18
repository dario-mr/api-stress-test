package com.dario.ast.util;

import static com.dario.ast.util.CopyUtil.deepCopy;
import static org.springframework.util.StringUtils.hasText;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.domain.RequestHeader;
import com.dario.ast.core.domain.RequestQueryParam;
import com.dario.ast.core.domain.RequestUriVariable;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EnvironmentUtil {

  public static ConfigParams applyEnvironmentVariables(ConfigParams configParams, Environment environment) {
    if (environment == null || environment.getVariables() == null || environment.getVariables().isEmpty()) {
      return configParams;
    }

    var envConfigParams = deepCopy(configParams, ConfigParams.class);

    environment.getVariables().forEach((key, value) -> {
      var envValue = value.getValue();
      applyToUrl(key, envValue, envConfigParams);
      applyToHeaders(key, envValue, envConfigParams.getHeaders());
      applyToUriVariables(key, envValue, envConfigParams.getUriVariables());
      applyToQueryParams(key, envValue, envConfigParams.getQueryParams());
      applyToRequestBody(key, envValue, envConfigParams);
    });

    return envConfigParams;
  }

  private static void applyToUrl(String envKey, String envValue, ConfigParams configParams) {
    if (!hasText(configParams.getUri())) {
      return;
    }

    var replacedUrl = configParams.getUri()
        .replace("{{%s}}".formatted(envKey), envValue);
    configParams.setUri(replacedUrl);
  }

  private static void applyToHeaders(String envKey, String envValue, Map<String, RequestHeader> headers) {
    if (headers == null) {
      return;
    }

    headers.forEach((headerKey, headerValue) -> {
      var replacedValue = headerValue.getValue().replace("{{%s}}".formatted(envKey), envValue);
      headerValue.setValue(replacedValue);
    });
  }

  private static void applyToUriVariables(String envKey, String envValue, Map<String, RequestUriVariable> uriVars) {
    if (uriVars == null) {
      return;
    }

    uriVars.forEach((uriVarKey, uriVarValue) -> {
      var replacedValue = uriVarValue.getValue().replace("{{%s}}".formatted(envKey), envValue);
      uriVarValue.setValue(replacedValue);
    });
  }

  private static void applyToQueryParams(String envKey, String envValue, Map<String, RequestQueryParam> queryParams) {
    if (queryParams == null) {
      return;
    }

    queryParams.forEach((paramKey, paramValue) -> {
      var replacedValue = paramValue.getValue().replace("{{%s}}".formatted(envKey), envValue);
      paramValue.setValue(replacedValue);
    });
  }

  private static void applyToRequestBody(String envKey, String envValue, ConfigParams configParams) {
    if (!hasText(configParams.getRequestBody())) {
      return;
    }

    var replacedBody = configParams.getRequestBody()
        .replace("{{%s}}".formatted(envKey), envValue);
    configParams.setRequestBody(replacedBody);
  }

}
