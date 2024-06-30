package com.dario.ast.core.domain;

import com.dario.ast.proxy.ApiRequest;
import com.dario.ast.proxy.ApiResponse;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.function.Consumer;

import static com.dario.ast.util.MapConverter.convertToMultiValueMap;

public record StressRequest(
        int numRequests,
        String uri,
        HttpMethod method,
        Map<String, String> headers,
        Map<String, String> uriVariables,
        Map<String, String> queryParams,
        String requestBody,
        Consumer<ApiResponse> resultConsumer
) {

    public ApiRequest toApiRequest() {
        return new ApiRequest(
                uri,
                method,
                convertToMultiValueMap(headers),
                uriVariables,
                convertToMultiValueMap(queryParams),
                requestBody
        );
    }
}
