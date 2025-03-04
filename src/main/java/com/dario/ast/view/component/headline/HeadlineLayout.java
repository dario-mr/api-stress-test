package com.dario.ast.view.component.headline;

import com.dario.ast.core.service.UserSessionService;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@CssImport(value = "./styles/headline-layout.css")
public class HeadlineLayout extends HorizontalLayout {

  public HeadlineLayout(UserSessionService userSessionService) {
    setWidthFull();
    setAlignItems(CENTER);

    var headline = new Headline();
    var userAvatar = new UserAvatar(userSessionService);

    // spacer component
    var spacer = new Div();
    spacer.getStyle().set("flex-grow", "1");

    // add all components
    add(headline, spacer, userAvatar);
  }
}
