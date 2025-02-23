package com.dario.ast.util;

import com.dario.ast.core.domain.ConfigParams;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.dario.ast.util.CurlPreviewUtil.buildCurlPreview;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

public class CurlPreviewUtilTest {

    @MethodSource("getBuildCurlPreviewParams")
    @ParameterizedTest
    void buildCurlPreview_whenParamsArePassed_shouldConvertToCurlPreview(ConfigParams params, String expectedPreview) {
        // when
        var actualPreview = buildCurlPreview(params);

        // then
        assertThat(actualPreview).isEqualTo(expectedPreview);
    }

    private static Stream<Arguments> getBuildCurlPreviewParams() {
        var uri = "https://www.api.com";
        var uriWithVar = "https://www.api.com/{uriVar}";

        var headers = new LinkedHashMap<String, String>();
        headers.put("header1", "headerValue1");
        headers.put("header2", "headerValue2");

        var uriVariables = Map.of("uriVar", "uriVarValue");

        var queryParams = new LinkedHashMap<String, String>();
        queryParams.put("queryParam1", "queryParamValue1");
        queryParams.put("queryParam2", "queryParamValue2");

        var requestBody = "{ \"requestBody\": \"requestBodyValue\" }";

        return Stream.of(
                of(ConfigParams.builder().build(), ""),
                of(null, ""),
                of(buildConfigParams(uri, POST, null, null, null, null), "curl -X POST 'https://www.api.com'"),
                of(buildConfigParams(uri, POST, null, null, null, requestBody),
                        """
                                curl -X POST 'https://www.api.com' \\
                                 -d '{ "requestBody": "requestBodyValue" }'"""),
                of(buildConfigParams(uri, POST, null, null, queryParams, requestBody),
                        """
                                curl -X POST 'https://www.api.com?queryParam1=queryParamValue1&queryParam2=queryParamValue2' \\
                                 -d '{ "requestBody": "requestBodyValue" }'"""),
                of(buildConfigParams(uriWithVar, GET, null, uriVariables, queryParams, requestBody),
                        """
                                curl -X GET 'https://www.api.com/uriVarValue?queryParam1=queryParamValue1&queryParam2=queryParamValue2' \\
                                 -d '{ "requestBody": "requestBodyValue" }'"""),
                of(buildConfigParams(uriWithVar, POST, headers, uriVariables, queryParams, requestBody),
                        """
                                curl -X POST 'https://www.api.com/uriVarValue?queryParam1=queryParamValue1&queryParam2=queryParamValue2' \\
                                 -H 'header1: headerValue1' \\
                                 -H 'header2: headerValue2' \\
                                 -d '{ "requestBody": "requestBodyValue" }'""")
        );
    }

    private static ConfigParams buildConfigParams(String uri, HttpMethod method, Map<String, String> headers,
                                                  Map<String, String> uriVariables, Map<String, String> queryParams, String requestBody) {
        return ConfigParams.builder()
                .uri(uri)
                .method(method)
                .headers(headers)
                .uriVariables(uriVariables)
                .queryParams(queryParams)
                .requestBody(requestBody)
                .build();
    }
}
