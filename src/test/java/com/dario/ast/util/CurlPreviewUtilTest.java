package com.dario.ast.util;

import static com.dario.ast.util.CurlPreviewUtil.buildCurlPreview;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RequestHeader;
import com.dario.ast.core.domain.RequestQueryParam;
import com.dario.ast.core.domain.RequestUriVariable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;

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

    var headers = new LinkedHashMap<String, RequestHeader>();
    headers.put("header1", RequestHeader.builder().value("headerValue1").build());
    headers.put("header2", RequestHeader.builder().value("headerValue2").build());

    var uriVariables = new LinkedHashMap<String, RequestUriVariable>();
    uriVariables.put("uriVar", RequestUriVariable.builder().value("uriVarValue").build());

    var queryParams = new LinkedHashMap<String, RequestQueryParam>();
    queryParams.put("queryParam1", RequestQueryParam.builder().value("queryParamValue1").build());
    queryParams.put("queryParam2", RequestQueryParam.builder().value("queryParamValue2").build());

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

  private static ConfigParams buildConfigParams(
      String uri,
      HttpMethod method,
      Map<String, RequestHeader> headers,
      Map<String, RequestUriVariable> uriVariables,
      Map<String, RequestQueryParam> queryParams,
      String requestBody
  ) {
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
