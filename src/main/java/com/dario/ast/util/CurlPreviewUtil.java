package com.dario.ast.util;

import static org.springframework.util.StringUtils.hasText;

import com.dario.ast.core.domain.ConfigParams;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CurlPreviewUtil {

  public static String buildCurlPreview(ConfigParams params) {
    if (params == null || !hasText(params.getUri())) {
      return "";
    }

    var previewBuilder = new StringBuilder();

    // replace URI variables
    final String[] uriArr = {params.getUri()};
    if (params.getUriVariables() != null) {
      params.getUriVariables().entrySet().stream()
          .filter(uriVar -> hasText(uriVar.getKey()) && hasText(uriVar.getValue().getValue()))
          .forEach(uriVar ->
              uriArr[0] = uriArr[0].replace("{%s}".formatted(uriVar.getKey()), uriVar.getValue().getValue()));
    }
    var uri = uriArr[0];

    // append http method
    if (params.getMethod() != null) {
      previewBuilder.append(params.getMethod().name());
    }

    // append uri
    previewBuilder.append(" '").append(uri);

    // append query parameters
    if (params.getQueryParams() != null) {
      final boolean[] isFirstQueryParam = {true};

      params.getQueryParams().entrySet().stream()
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
    if (params.getHeaders() != null) {
      params.getHeaders().entrySet().stream()
          .filter(header -> hasText(header.getKey()) && hasText(header.getValue().getValue()))
          .forEach(header -> previewBuilder
              .append(" \\\n")
              .append(" -H '")
              .append(header.getKey()).append(": ").append(header.getValue().getValue())
              .append("'"));
    }

    // append request body
    if (hasText(params.getRequestBody())) {
      previewBuilder
          .append(" \\\n")
          .append(" -d '")
          .append(params.getRequestBody())
          .append("'");
    }

    // if no item in the preview builder, return empty string
    if (previewBuilder.isEmpty()) {
      return "";
    }

    return "curl -X " + previewBuilder;
  }

}
