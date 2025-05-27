package com.dario.ast.view.component.run;

import com.dario.ast.core.domain.IndexedApiResponse;
import com.dario.ast.event.stresstest.ApiRequestCompletedEvent;
import com.dario.ast.event.stresstest.StressTestStartedEvent;
import com.dario.ast.proxy.api.dto.ApiResponse;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;
import java.util.List;
import org.vaadin.klaudeta.PaginatedGrid;

@UIScope
@SpringComponent
@CssImport(value = "./styles/result-grid.css")
public class ResultGrid extends PaginatedGrid<IndexedApiResponse, Void> {

  private final ListDataProvider<IndexedApiResponse> dataProvider = new ListDataProvider<>(new ArrayList<>());

  public ResultGrid() {
    addClassName("result-grid");

    // columns
    addColumn(IndexedApiResponse::index)
        .setHeader("#")
        .setWidth("4.5em")
        .setFlexGrow(0);
    addColumn(r -> r.response().statusCode())
        .setHeader("Status")
        .setWidth("12em")
        .setFlexGrow(0);
    addColumn(r -> r.response().responseTimeMs() + " ms")
        .setHeader("Response Time")
        .setAutoWidth(true)
        .setFlexGrow(0);
    addColumn(r -> r.response().responseBody())
        .setHeader("Response")
        .setFlexGrow(1)
        .setClassNameGenerator(item -> "response-column");

    // on click: show detail dialog
    addItemClickListener(event -> {
      var detail = new ResultDetail(event.getItem());
      detail.open();
    });

    // paging
    setPageSize(50);
    setPaginatorSize(5);

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
    var items = (List<IndexedApiResponse>) dataProvider.getItems();
    int newIndex = items.size() + 1;
    items.add(new IndexedApiResponse(newIndex, apiResponse));
    dataProvider.refreshAll();
  }

}
