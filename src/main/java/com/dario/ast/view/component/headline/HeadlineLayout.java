package com.dario.ast.view.component.headline;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;

import com.dario.ast.view.component.environment.EnvironmentCombo;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent
@CssImport(value = "./styles/headline-layout.css")
public class HeadlineLayout extends HorizontalLayout {

  public HeadlineLayout(UserAvatar userAvatar, EnvironmentCombo environmentCombo) {
    setWidthFull();
    setAlignItems(CENTER);

    var headline = new Headline();

    // spacer component
    var spacer = new Div();
    spacer.getStyle().set("flex-grow", "1");

    // add all components
    add(headline, spacer, environmentCombo, userAvatar);
  }
}
