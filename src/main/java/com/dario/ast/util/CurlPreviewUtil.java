package com.dario.ast.util;

import static com.dario.ast.util.EnvironmentUtil.applyEnvVarsToConfigParams;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpMethod.GET;
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

    String uri = buildUriWithVariables(envConfigParams);
    String queryString = buildQueryString(envConfigParams);
    StringBuilder previewBuilder = new StringBuilder("curl");

    // only add -X if not GET
    if (envConfigParams.getMethod() != null && !GET.equals(envConfigParams.getMethod())) {
      previewBuilder.append(" -X ").append(envConfigParams.getMethod().name());
    }

    previewBuilder.append(" '").append(uri);
    if (!queryString.isEmpty()) {
      previewBuilder.append("?").append(queryString);
    }
    previewBuilder.append("'");

    // headers
    if (envConfigParams.getHeaders() != null) {
      envConfigParams.getHeaders().entrySet().stream()
          .filter(header -> hasText(header.getKey()) && hasText(header.getValue().getValue()))
          .forEach(header -> previewBuilder.append(formatHeader(header.getKey(), header.getValue().getValue())));
    }

    // request body
    if (hasText(envConfigParams.getRequestBody())) {
      previewBuilder
          .append(" \\\n  -d '")
          .append(escapeSingleQuotes(envConfigParams.getRequestBody()))
          .append("'");
    }

    return previewBuilder.toString();
  }

  private static String buildUriWithVariables(ConfigParams params) {
    var uri = params.getUri();
    var uriVariables = params.getUriVariables();
    if (uriVariables == null || uriVariables.isEmpty()) {
      return uri;
    }

    for (var entry : uriVariables.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue().getValue();
      if (hasText(key) && hasText(value)) {
        uri = uri.replace("{%s}".formatted(key), value);
      }
    }

    return uri;
  }

  private static String buildQueryString(ConfigParams params) {
    var queryParams = params.getQueryParams();
    if (queryParams == null || queryParams.isEmpty()) {
      return "";
    }

    return queryParams.entrySet().stream()
        .filter(qp -> hasText(qp.getKey()) && hasText(qp.getValue().getValue()))
        .map(qp -> qp.getKey() + "=" + qp.getValue().getValue())
        .collect(joining("&"));
  }

  private static String formatHeader(String key, String value) {
    return " \\\n  -H '" + escapeSingleQuotes(key) + ": " + escapeSingleQuotes(value) + "'";
  }

  private static String escapeSingleQuotes(String value) {
    return value == null ? "" : value.replace("'", "'\"'\"'");
  }
}