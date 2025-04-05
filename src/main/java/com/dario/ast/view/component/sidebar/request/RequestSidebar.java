package com.dario.ast.view.component.sidebar.request;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent
@CssImport(value = "./styles/sidebar.css")
public class RequestSidebar extends VerticalLayout {

  public RequestSidebar(CreateRequestButton createRequestButton, RequestGrid requestGrid) {
    addClassNames("card-layout", "sidebar-layout");
    setPadding(false);
    setSpacing(false);
    setWidthFull();

    var title = new H4("Requests");
    title.getStyle().set("padding", "var(--lumo-space-m)");

    add(title, createRequestButton, requestGrid);
  }
}
