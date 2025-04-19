package com.dario.ast.view.component.request;

import com.dario.ast.view.component.config.ConfigLayout;
import com.dario.ast.view.component.run.RunLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent
public class RequestLayout extends VerticalLayout {

  public RequestLayout(ConfigLayout configLayout, RunLayout runLayout) {
    getStyle().set("gap", "var(--lumo-space-s)");
    setPadding(false);

    add(configLayout, runLayout);
  }

}
