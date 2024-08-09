package com.dario.ast.util;

import com.dario.ast.core.domain.StressTestParams;
import lombok.experimental.UtilityClass;

import static java.lang.String.format;

@UtilityClass
public class PreviewUtil {

    public static String buildCurlPreview(StressTestParams params) {
        if (params == null) {
            return "";
        }

        var previewBuilder = new StringBuilder();

        // replace uri variables in uri
        var uri = params.getUri();
        if (params.getUriVariables() != null) {
            for (var uriVar : params.getUriVariables().entrySet()) {
                uri = uri.replace(format("{%s}", uriVar.getKey()), uriVar.getValue());
            }
        }

        // append http method
        if (params.getMethod() != null) {
            previewBuilder.append(params.getMethod().name());
        }

        // append uri
        if (uri != null) {
            previewBuilder.append(" '").append(uri);
        }

        // append query parameters
        if (params.getQueryParams() != null) {
            boolean isFirstQueryParam = true;

            for (var queryParam : params.getQueryParams().entrySet()) {
                if (isFirstQueryParam) {
                    previewBuilder.append("?");
                    isFirstQueryParam = false;
                } else {
                    previewBuilder.append("&");
                }
                previewBuilder.append(queryParam.getKey()).append("=").append(queryParam.getValue());
            }
        }

        // close uri
        if (uri != null) {
            previewBuilder.append("'");
        }

        // append headers
        if (params.getHeaders() != null) {
            for (var header : params.getHeaders().entrySet()) {
                previewBuilder
                        .append(" \\\n")
                        .append(" -H '")
                        .append(header.getKey()).append(": ").append(header.getValue())
                        .append("'");
            }
        }

        // append request body
        if (params.getRequestBody() != null && !params.getRequestBody().isEmpty()) {
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
