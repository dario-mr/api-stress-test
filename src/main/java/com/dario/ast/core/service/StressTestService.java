package com.dario.ast.core.service;

import static com.dario.ast.util.MapUtil.convertToMultiValueMap;
import static com.dario.ast.util.MapUtil.flatEntryValueMap;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.proxy.ApiProxy;
import com.dario.ast.proxy.ApiRequest;
import com.dario.ast.proxy.ApiResponse;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StressTestService {

  private final ApiProxy apiProxy;
  private ExecutorService executor;

  public void startStressTest(ConfigParams configParams, RunParams runParams, Consumer<ApiResponse> responseConsumer,
      ExecutorService executor) {
    this.executor = executor;
    var apiRequest = toApiRequest(configParams);

    for (int i = 0; i < runParams.getNumRequests(); i++) {
      supplyAsync(() -> apiProxy.makeRequest(apiRequest), executor)
          .thenAccept(responseConsumer);
    }
  }

  public void cancelStressTest() {
    if (executor != null) {
      executor.shutdownNow();
    }
  }

  private ApiRequest toApiRequest(ConfigParams configParams) {
    return new ApiRequest(
        configParams.getUri(),
        configParams.getMethod(),
        convertToMultiValueMap(flatEntryValueMap(configParams.getHeaders())),
        flatEntryValueMap(configParams.getUriVariables()),
        convertToMultiValueMap(flatEntryValueMap(configParams.getQueryParams())),
        configParams.getRequestBody()
    );
  }

}
