package com.dario.ast.view.component.run;

import com.dario.ast.event.ApiRequestCompletedEvent;
import com.dario.ast.event.StressTestStartedEvent;
import com.dario.ast.proxy.api.dto.ApiResponse;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;

@UIScope
@SpringComponent
@CssImport(value = "./styles/result-grid.css")
public class ResultGrid extends Grid<ApiResponse> {

  // TODO paging: https://vaadin.com/directory/component/flow-viritin or https://vaadin.com/directory/component/beantable
  // TODO open dialog on click?

  private final ListDataProvider<ApiResponse> dataProvider = new ListDataProvider<>(new ArrayList<>());

  public ResultGrid() {
    addClassName("result-grid");

    // columns
    addColumn(ApiResponse::statusCode)
        .setHeader("Status")
        .setWidth("12em")
        .setFlexGrow(0);
    addColumn(apiResponse -> apiResponse.responseTimeMs() + " ms")
        .setHeader("Response Time")
        .setAutoWidth(true)
        .setFlexGrow(0);
    addColumn(ApiResponse::responseBody)
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
        event -> clearGrid()
    );

    ComponentUtil.addListener(attachEvent.getUI(),
        ApiRequestCompletedEvent.class,
        event -> addApiResponse(event.getApiResponse())
    );
  }

  private void clearGrid() {
    dataProvider.getItems().clear();
    dataProvider.refreshAll();
  }

  private void addApiResponse(ApiResponse apiResponse) {
    dataProvider.getItems().add(apiResponse);
    dataProvider.refreshAll();
  }

}
