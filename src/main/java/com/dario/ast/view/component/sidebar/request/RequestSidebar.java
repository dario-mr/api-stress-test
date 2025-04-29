package com.dario.ast.view.component.sidebar.request;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent
@CssImport(value = "./styles/sidebar.css")
public class RequestSidebar extends VerticalLayout {

  public RequestSidebar(CreateLayout createLayout, RequestGrid requestGrid) {
    addClassNames("card-layout", "sidebar-layout");
    setPadding(false);
    setSpacing(false);
    setWidthFull();

    add(createLayout, requestGrid);
  }
}
