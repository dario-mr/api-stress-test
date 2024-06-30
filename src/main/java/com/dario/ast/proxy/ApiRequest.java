package com.dario.ast.proxy;

import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public record ApiRequest(
        String uri,
        HttpMethod httpMethod,
        MultiValueMap<String, String> headers,
        Map<String, String> uriVariables,
        MultiValueMap<String, String> queryParams,
        String requestBody
) {
}
