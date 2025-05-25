package com.dario.ast.core.service;

import static com.dario.ast.util.EnvironmentUtil.applyEnvVarsToConfigParams;
import static java.util.concurrent.Executors.newFixedThreadPool;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.proxy.api.dto.ApiResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StressTestOrchestrator {

  private final PreRequestService prerequestService;
  private final StressTestRunner stressTestRunner;
  private final AppState appState;

  private ExecutorService executor;

  public void startStressTest(
      ConfigParams configParams,
      RunParams runParams,
      Consumer<ApiResponse> onResponse,
      Runnable onComplete,
      Consumer<Throwable> onError
  ) {
    new Thread(() -> {
      try {
        prerequestService.runAndApplyPreRequests();

        var selectedEnvironment = appState.getSelectedEnvironment();
        var envConfigParams = applyEnvVarsToConfigParams(configParams, selectedEnvironment);

        executor = newFixedThreadPool(runParams.getThreadPoolSize());
        var numRequests = runParams.getNumRequests();
        var completedCount = new AtomicInteger(0);

        stressTestRunner.run(
            envConfigParams,
            runParams,
            response -> {
              onResponse.accept(response);
              if (completedCount.incrementAndGet() == numRequests) {
                shutdownExecutor();
                onComplete.run();
              }
            },
            executor
        );
      } catch (Exception ex) {
        onError.accept(ex);
        shutdownNowExecutor();
      }
    }).start();
  }

  public void cancelStressTest() {
    shutdownNowExecutor();
  }

  private void shutdownExecutor() {
    if (executor != null) {
      executor.shutdown();
      executor = null;
    }
  }

  private void shutdownNowExecutor() {
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
    }
  }

}
