package com.dario.ast.util;

import static com.dario.ast.util.EnvironmentUtil.applyEnvVarsToConfigParams;
import static org.springframework.util.StringUtils.hasText;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Environment;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CurlPreviewUtil {

  public static String buildCurlPreview(ConfigParams configParams, Environment environment) {
    if (configParams == null || !hasText(configParams.getUri())) {
      return "";
    }

    var envConfigParams = applyEnvVarsToConfigParams(configParams, environment);
    var previewBuilder = new StringBuilder();

    // replace URI variables
    final String[] uriArr = {envConfigParams.getUri()};
    if (envConfigParams.getUriVariables() != null) {
      envConfigParams.getUriVariables().entrySet().stream()
          .filter(uriVar -> hasText(uriVar.getKey()) && hasText(uriVar.getValue().getValue()))
          .forEach(uriVar ->
              uriArr[0] = uriArr[0].replace("{%s}".formatted(uriVar.getKey()), uriVar.getValue().getValue()));
    }
    var uri = uriArr[0];

    // append http method
    if (envConfigParams.getMethod() != null) {
      previewBuilder.append(envConfigParams.getMethod().name());
    }

    // append uri
    previewBuilder.append(" '").append(uri);

    // append query parameters
    if (envConfigParams.getQueryParams() != null) {
      final boolean[] isFirstQueryParam = {true};

      envConfigParams.getQueryParams().entrySet().stream()
          .filter(queryParam -> hasText(queryParam.getKey()) && hasText(queryParam.getValue().getValue()))
          .forEach(queryParam -> {
            if (isFirstQueryParam[0]) {
              previewBuilder.append("?");
              isFirstQueryParam[0] = false;
            } else {
              previewBuilder.append("&");
            }
            previewBuilder.append(queryParam.getKey()).append("=").append(queryParam.getValue().getValue());
          });
    }

    // close uri
    previewBuilder.append("'");

    // append headers
    if (envConfigParams.getHeaders() != null) {
      envConfigParams.getHeaders().entrySet().stream()
          .filter(header -> hasText(header.getKey()) && hasText(header.getValue().getValue()))
          .forEach(header -> previewBuilder
              .append(" \\\n")
              .append(" -H '")
              .append(header.getKey()).append(": ").append(header.getValue().getValue())
              .append("'"));
    }

    // append request body
    if (hasText(envConfigParams.getRequestBody())) {
      previewBuilder
          .append(" \\\n")
          .append(" -d '")
          .append(envConfigParams.getRequestBody())
          .append("'");
    }

    // if no item in the preview builder, return empty string
    if (previewBuilder.isEmpty()) {
      return "";
    }

    return "curl -X " + previewBuilder;
  }

}
