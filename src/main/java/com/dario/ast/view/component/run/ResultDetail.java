package com.dario.ast.view.component.run;

import static com.dario.ast.util.JsonUtil.prettifyJson;
import static com.vaadin.flow.component.icon.VaadinIcon.CLOSE_CIRCLE;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.STRETCH;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN;

import com.dario.ast.core.domain.IndexedApiResponse;
import com.dario.ast.proxy.api.dto.ApiResponse;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

@CssImport(value = "./styles/result-detail.css")
@CssImport(value = "./styles/vaadin-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
public class ResultDetail extends Dialog {

  public ResultDetail(IndexedApiResponse indexedResponse) {
    setWidthFull();
    setMaxWidth("80em");

    var response = indexedResponse.response();

    var titleRow = buildTitleRow(indexedResponse.index());
    var infoRow = buildInfoRow(response);
    var responseBody = buildResponseBody(response);

    var container = buildContainer(titleRow, infoRow, responseBody);
    container.addClassName("result-detail");

    add(container);
  }

  private HorizontalLayout buildTitleRow(int index) {
    var title = new H4("Result #%s".formatted(index));

    var closeButton = new Button(CLOSE_CIRCLE.create());
    closeButton.addClickListener(event -> close());
    closeButton.getStyle().set("margin", "0");

    var titleRow = new HorizontalLayout(title, closeButton);
    titleRow.setWidthFull();
    titleRow.setJustifyContentMode(BETWEEN);
    titleRow.setAlignItems(CENTER);
    titleRow.setPadding(false);
    titleRow.setSpacing(false);

    return titleRow;
  }

  private HorizontalLayout buildInfoRow(ApiResponse response) {
    var statusText = new TextField("Status", response.statusCode().toString(), "");
    statusText.setReadOnly(true);
    statusText.setWidthFull();

    var responseTime = new TextField("Response Time", response.responseTimeMs() + " ms", "");
    responseTime.setReadOnly(true);
    responseTime.setWidthFull();

    // index, status, and response time on the same row
    var infoRow = new HorizontalLayout(statusText, responseTime);
    infoRow.setWidthFull();
    infoRow.setSpacing(true);
    infoRow.setPadding(false);
    infoRow.setAlignItems(STRETCH);
    infoRow.setFlexGrow(1, statusText, responseTime);

    return infoRow;
  }

  private TextArea buildResponseBody(ApiResponse response) {
    var responseBody = new TextArea("Response", prettifyJson(response.responseBody()), "");
    responseBody.setReadOnly(true);
    responseBody.setWidthFull();

    return responseBody;
  }

  private VerticalLayout buildContainer(HorizontalLayout closeButton, HorizontalLayout infoRow,
      TextArea responseBody) {
    var container = new VerticalLayout(closeButton, infoRow, responseBody);
    container.setWidthFull();
    container.setSpacing(false);
    container.getStyle().set("padding", "var(--lumo-space-s)");

    return container;
  }

}
