package com.dario.ast.view.component.run;

import static org.springframework.util.StringUtils.hasText;

import com.dario.ast.event.ApiRequestCompletedEvent;
import com.dario.ast.event.StressTestStartedEvent;
import com.dario.ast.proxy.api.dto.ApiResponse;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;

@UIScope
@SpringComponent
public class ResultGrid extends Grid<ApiResponse> {

  // TODO fix color
  // TODO fix horizontal and vertical scrolling

  private final ListDataProvider<ApiResponse> dataProvider = new ListDataProvider<>(new ArrayList<>());

  public ResultGrid() {
    // columns
    addColumn(ApiResponse::statusCode)
        .setHeader("Status")
        .setWidth("12em")
        .setFlexGrow(0);
    addColumn(apiResponse -> apiResponse.responseTimeMs() + " ms")
        .setHeader("Response Time")
        .setAutoWidth(true)
        .setFlexGrow(0);
    addColumn(apiResponse ->
        hasText(apiResponse.errorMessage())
            ? apiResponse.errorMessage()
            : apiResponse.responseBody())
        .setHeader("Response")
        .setAutoWidth(true)
        .setFlexGrow(1);

    setDataProvider(dataProvider);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    ComponentUtil.addListener(attachEvent.getUI(),
        StressTestStartedEvent.class,
        event -> {
          dataProvider.getItems().clear();
          dataProvider.refreshAll();
        }
    );

    ComponentUtil.addListener(attachEvent.getUI(),
        ApiRequestCompletedEvent.class,
        event -> addApiResponse(event.getApiResponse())
    );
  }

  private void addApiResponse(ApiResponse apiResponse) {
    dataProvider.getItems().add(apiResponse);
    dataProvider.refreshAll();
  }

}
