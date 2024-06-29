package com.dario.ast.core.service;

import com.dario.ast.core.domain.StressRequest;
import com.dario.ast.proxy.ApiProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.dario.ast.util.MapConverter.convert;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@RequiredArgsConstructor
public class StressService {

    private final ApiProxy apiProxy;
    private final List<Future<?>> futureRequests = new ArrayList<>();

    public void startStressTest(StressRequest request) {
        for (int i = 0; i < request.numRequests(); i++) {
            var futureRequest = supplyAsync(
                    () -> apiProxy.makeRequest(request.url(), request.method(), convert(request.headers()), request.uriVariables(), convert(request.queryParams())),
                    Executors.newFixedThreadPool(request.threadPoolSize())
            ).thenAccept(request.resultConsumer());

            futureRequests.add(futureRequest);
        }
    }

    public void cancelAllRequests() {
        for (Future<?> future : futureRequests) {
            future.cancel(true);
        }
        futureRequests.clear();
    }
}
