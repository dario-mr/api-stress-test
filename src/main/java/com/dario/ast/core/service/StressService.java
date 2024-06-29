package com.dario.ast.core.service;

import com.dario.ast.proxy.ApiProxy;
import com.dario.ast.proxy.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.dario.ast.util.MapConverter.convert;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@RequiredArgsConstructor
public class StressService {

    private final ApiProxy apiProxy;
    private final ExecutorService executor;

    private final List<Future<?>> futureRequests = new ArrayList<>();

    public void sendRequests(int numRequests,
                             String url,
                             HttpMethod method,
                             Map<String, String> headers,
                             Map<String, String> uriVariables,
                             Map<String, String> queryParams,
                             Consumer<ApiResponse> resultConsumer) {
        for (int i = 0; i < numRequests; i++) {
            var futureRequest = supplyAsync(() -> apiProxy.makeRequest(url, method, convert(headers), uriVariables, convert(queryParams)), executor)
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
