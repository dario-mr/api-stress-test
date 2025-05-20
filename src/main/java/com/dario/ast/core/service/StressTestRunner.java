package com.dario.ast.core.service;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.proxy.api.ApiProxy;
import com.dario.ast.proxy.api.dto.ApiResponse;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StressTestRunner {

  private final ApiProxy apiProxy;

  public void run(ConfigParams configParams, RunParams runParams,
      Consumer<ApiResponse> responseConsumer, ExecutorService executor) {
    var apiRequest = configParams.toApiRequest();

    for (int i = 0; i < runParams.getNumRequests(); i++) {
      supplyAsync(() -> apiProxy.makeRequest(apiRequest), executor)
          .thenAccept(responseConsumer);
    }
  }


}
