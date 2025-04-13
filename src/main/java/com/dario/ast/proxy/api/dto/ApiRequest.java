package com.dario.ast.proxy.api.dto;

import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

public record ApiRequest(
    String uri,
    HttpMethod httpMethod,
    MultiValueMap<String, String> headers,
    Map<String, String> uriVariables,
    MultiValueMap<String, String> queryParams,
    String requestBody
) {

}
