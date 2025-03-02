package com.dario.ast.core.service;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
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
public class AstStorageService { // TODO refactor to access DB

    private static final String STRESS_TEST_PARAMS_KEY = "stressTestParams";

    private final ObjectMapper objectMapper;

    public void saveAstRequest(AstRequest astRequest) throws JsonProcessingException {
        var encodedRequest = objectMapper.writeValueAsString(astRequest);
        WebStorage.setItem(STRESS_TEST_PARAMS_KEY, encodedRequest);
    }

    public CompletableFuture<AstRequest> getAstRequest() {
        var astRequestFuture = new CompletableFuture<AstRequest>();

        WebStorage.getItem(STRESS_TEST_PARAMS_KEY, jsonParams -> {
            var astRequest = new AstRequest();

            if (jsonParams != null) {
                try {
                    astRequest = objectMapper.readValue(jsonParams, AstRequest.class);
                } catch (JsonProcessingException e) {
                    astRequestFuture.completeExceptionally(e);
                }
            }

            applyDefaultValues(astRequest);
            astRequestFuture.complete(astRequest);
        });

        return astRequestFuture;
    }

    private static void applyDefaultValues(AstRequest astRequest) {
        var configParams = astRequest.getConfigParams();
        var runParams = astRequest.getRunParams();

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
        astRequest.setConfigParams(configParams);

        if (runParams == null) {
            runParams = new RunParams();
        }
        if (runParams.getNumRequests() == 0) {
            runParams.setNumRequests(10);
        }
        if (runParams.getThreadPoolSize() == 0) {
            runParams.setThreadPoolSize(4);
        }
        astRequest.setRunParams(runParams);
    }
}
