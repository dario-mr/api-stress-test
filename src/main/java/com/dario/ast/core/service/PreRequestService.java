package com.dario.ast.core.service;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.ConfigParams;
import java.util.ArrayList;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreRequestService {

  private final AppState appState;

  public void updatePreRequestInAppState(ConfigParams updatedPreRequestParams) {
    var currentPreRequestsParams = new ArrayList<>(appState.getPreRequestsParams());

    currentPreRequestsParams.replaceAll(preRequestParams ->
        Objects.equals(preRequestParams.getRequestId(), updatedPreRequestParams.getRequestId())
            ? updatedPreRequestParams
            : preRequestParams
    );

    appState.setPreRequestsParams(currentPreRequestsParams);
  }

}
