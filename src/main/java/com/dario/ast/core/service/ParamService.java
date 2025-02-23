package com.dario.ast.core.service;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
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
        var configParams = params.getConfigParams();
        var runParams = params.getRunParams();

        if (configParams == null) {
            configParams = new ConfigParams();
        }
        if (configParams.getUri() == null) {
            configParams.setUri("");
        }
        if (configParams.getMethod() == null) {
            configParams.setMethod(GET);
        }
        if (configParams.getHeaders() == null) {
            configParams.setHeaders(new HashMap<>());
        }
        if (configParams.getUriVariables() == null) {
            configParams.setUriVariables(new HashMap<>());
        }
        if (configParams.getQueryParams() == null) {
            configParams.setQueryParams(new HashMap<>());
        }
        if (configParams.getRequestBody() == null) {
            configParams.setRequestBody("");
        }
        params.setConfigParams(configParams);

        if (runParams == null) {
            runParams = new RunParams();
        }
        if (runParams.getNumRequests() == 0) {
            runParams.setNumRequests(10);
        }
        if (runParams.getThreadPoolSize() == 0) {
            runParams.setThreadPoolSize(4);
        }
        params.setRunParams(runParams);
    }
}
