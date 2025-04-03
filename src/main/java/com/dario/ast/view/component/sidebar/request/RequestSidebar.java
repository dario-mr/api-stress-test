package com.dario.ast.view.component.sidebar.request;

import static com.dario.ast.view.component.sidebar.MainSidebar.MAX_SIDEBAR_WIDTH;
import static com.dario.ast.view.component.sidebar.MainSidebar.MIN_SIDEBAR_WIDTH;

import com.vaadin.flow.component.dependency.CssImport;
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
    setMinWidth(MIN_SIDEBAR_WIDTH);
    setMaxWidth(MAX_SIDEBAR_WIDTH);

    add(createRequestButton, requestGrid);
  }
}
