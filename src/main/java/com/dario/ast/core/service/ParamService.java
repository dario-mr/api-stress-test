package com.dario.ast.core.service;

import com.dario.ast.core.domain.StressTestParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.page.WebStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpMethod.GET;

@Service
@RequiredArgsConstructor
public class ParamService {

    private static final String STRESS_TEST_PARAMS_KEY = "stressTestParams";

    private final ObjectMapper objectMapper;

    public void saveParams(StressTestParams params) throws JsonProcessingException {
        var encodedRequest = objectMapper.writeValueAsString(params);
        WebStorage.setItem(STRESS_TEST_PARAMS_KEY, encodedRequest);
    }

    public CompletableFuture<StressTestParams> getParams() {
        var futureParams = new CompletableFuture<StressTestParams>();

        WebStorage.getItem(STRESS_TEST_PARAMS_KEY, jsonParams -> {
            var params = new StressTestParams();

            if (jsonParams != null) {
                try {
                    params = objectMapper.readValue(jsonParams, StressTestParams.class);
                } catch (JsonProcessingException e) {
                    futureParams.completeExceptionally(e);
                }
            }

            applyDefaultValues(params);
            futureParams.complete(params);
        });

        return futureParams;
    }

    private static void applyDefaultValues(StressTestParams params) {
        if (params.getUri() == null) {
            params.setUri("");
        }
        if (params.getMethod() == null) {
            params.setMethod(GET);
        }
        if (params.getHeaders() == null) {
            params.setHeaders(new HashMap<>());
        }
        if (params.getUriVariables() == null) {
            params.setUriVariables(new HashMap<>());
        }
        if (params.getQueryParams() == null) {
            params.setQueryParams(new HashMap<>());
        }
        if (params.getRequestBody() == null) {
            params.setRequestBody("");
        }
        if (params.getNumRequests() == 0) {
            params.setNumRequests(10);
        }
        if (params.getThreadPoolSize() == 0) {
            params.setThreadPoolSize(4);
        }
    }
}
