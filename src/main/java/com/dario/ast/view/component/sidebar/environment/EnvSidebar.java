package com.dario.ast.view.component.sidebar.environment;

import static com.dario.ast.view.component.sidebar.MainSidebar.MAX_SIDEBAR_WIDTH;
import static com.dario.ast.view.component.sidebar.MainSidebar.MIN_SIDEBAR_WIDTH;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent
@CssImport(value = "./styles/sidebar.css")
public class EnvSidebar extends VerticalLayout {

  public EnvSidebar(CreateEnvButton createEnvButton, EnvGrid envGrid) {
    addClassNames("card-layout", "sidebar-layout");
    setPadding(false);
    setSpacing(false);
    setWidthFull();
    setMinWidth(MIN_SIDEBAR_WIDTH);
    setMaxWidth(MAX_SIDEBAR_WIDTH);

    var title = new H4("Environments");
    title.getStyle().set("padding", "var(--lumo-space-m)");

    add(title, createEnvButton, envGrid);
  }
}
