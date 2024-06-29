package com.dario.ast.core.domain;

import com.dario.ast.proxy.ApiResponse;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.function.Consumer;

public record StressRequest(
        int threadPoolSize,
        int numRequests,
        String url,
        HttpMethod method,
        Map<String, String> headers,
        Map<String, String> uriVariables,
        Map<String, String> queryParams,
        Consumer<ApiResponse> resultConsumer
) {
}
