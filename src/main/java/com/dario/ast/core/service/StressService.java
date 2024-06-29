package com.dario.ast.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.springframework.http.HttpMethod.GET;

@Service
@RequiredArgsConstructor
public class StressService {

    private final ApiService apiService;
    private final ExecutorService executor;

    private final List<Future<?>> futureRequests = new ArrayList<>();

    public void sendRequests(int numRequests, Consumer<HttpStatus> resultConsumer) {
        for (int i = 0; i < numRequests; i++) {
            Future<?> futureRequest = supplyAsync(() -> apiService.makeRequest(GET, "https://www.google.com", null, null), executor)
                    .thenAccept(resultConsumer);
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
