package com.dario.ast.core.service;

import com.dario.ast.core.domain.StressTestParams;
import com.dario.ast.proxy.ApiProxy;
import com.dario.ast.proxy.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@RequiredArgsConstructor
public class StressService {

    private final ApiProxy apiProxy;
    private ExecutorService executor;

    public void startStressTest(StressTestParams stressTestParams, Consumer<ApiResponse> responseConsumer, ExecutorService executor) {
        this.executor = executor;

        for (int i = 0; i < stressTestParams.getNumRequests(); i++) {
            supplyAsync(() -> apiProxy.makeRequest(stressTestParams.toApiRequest()), executor)
                    .thenAccept(responseConsumer);
        }
    }

    public void cancelStressTest() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
