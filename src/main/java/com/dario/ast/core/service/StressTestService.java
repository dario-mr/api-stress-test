package com.dario.ast.core.service;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.proxy.ApiProxy;
import com.dario.ast.proxy.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@RequiredArgsConstructor
public class StressTestService {

    private final ApiProxy apiProxy;
    private ExecutorService executor;

    public void startStressTest(AstRequest astRequest, Consumer<ApiResponse> responseConsumer, ExecutorService executor) {
        this.executor = executor;

        for (int i = 0; i < astRequest.getRunParams().getNumRequests(); i++) {
            supplyAsync(() -> apiProxy.makeRequest(astRequest.getConfigParams().toApiRequest()), executor)
                    .thenAccept(responseConsumer);
        }
    }

    public void cancelStressTest() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
