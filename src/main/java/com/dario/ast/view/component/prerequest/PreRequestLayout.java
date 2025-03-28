package com.dario.ast.view.component.prerequest;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent
@CssImport(value = "./styles/pre-request-layout.css")
public class PreRequestLayout extends VerticalLayout {

  public PreRequestLayout() {
    addClassNames("card-layout", "pre-request-layout");
    setWidthFull();

    add(
        new H4("Pre-Requests")
    );
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
  }

}
