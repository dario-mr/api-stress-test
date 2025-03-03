package com.dario.ast.view.component.headline;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.core.service.UserSessionService;
import com.dario.ast.view.component.save.SaveButton;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.function.Supplier;

@CssImport(value = "./styles/headline-layout.css")
public class HeadlineLayout extends HorizontalLayout {

  public HeadlineLayout(AstRequestService astRequestService, UserSessionService userSessionService,
      Supplier<ConfigParams> configParamsSupplier, Supplier<RunParams> runParamsSupplier) {
    setWidthFull();
    setAlignItems(CENTER);

    var headline = new Headline();
    var saveButton = new SaveButton(astRequestService, configParamsSupplier, runParamsSupplier);
    var userAvatar = new UserAvatar(userSessionService);

    // spacer component
    var spacer = new Div();
    spacer.getStyle().set("flex-grow", "1");

    // add all components
    add(headline, spacer, saveButton, userAvatar);
  }
}
