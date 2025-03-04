package com.dario.ast.view.component.sidebar;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

@UIScope
@SpringComponent
@CssImport(value = "./styles/sidebar.css")
public class Sidebar extends VerticalLayout {

  private final static String MAX_SIDEBAR_WIDTH = "250px";

  @Autowired
  public Sidebar(CreateButton createButton, RequestGrid requestGrid) {
    setWidthFull();
    setPadding(false);
    setSpacing(false);
    setMaxWidth(MAX_SIDEBAR_WIDTH);
    addClassNames("card-layout", "sidebar-layout");

    // title + create button
    var title = new H4("Requests");
    title.addClassName("sidebar-title");

    var titleCreateLayout = new HorizontalLayout(title, createButton);
    titleCreateLayout.setWidthFull();
    titleCreateLayout.setFlexGrow(1, title);

    add(titleCreateLayout, requestGrid);
  }
}
