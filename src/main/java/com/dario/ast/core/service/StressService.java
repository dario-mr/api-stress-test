package com.dario.ast.core.service;

import com.dario.ast.core.domain.StressRequest;
import com.dario.ast.proxy.ApiProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@RequiredArgsConstructor
public class StressService {

    private final ApiProxy apiProxy;
    private ExecutorService executor;

    public void startStressTest(StressRequest request, ExecutorService executor) {
        this.executor = executor;

        for (int i = 0; i < request.numRequests(); i++) {
            supplyAsync(() -> apiProxy.makeRequest(request.toApiRequest()), executor)
                    .thenAccept(request.resultConsumer());
        }
    }

    public void cancelStressTest() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
