package com.dario.ast.view.component.headline;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;

import com.dario.ast.core.domain.ApplicationState;
import com.dario.ast.core.domain.OAuthUser;
import com.dario.ast.core.service.AstUserService;
import com.dario.ast.core.service.UserSessionService;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

@UIScope
@SpringComponent
public class UserAvatar extends Div {

  private final UserSessionService userSessionService;
  private final AstUserService astUserService;
  private final ApplicationState applicationState;

  @Autowired
  public UserAvatar(UserSessionService userSessionService,
      AstUserService astUserService,
      ApplicationState applicationState) {
    this.userSessionService = userSessionService;
    this.astUserService = astUserService;
    this.applicationState = applicationState;
    loadCurrentUser(); // this is the first bean that is loaded and that has a user session... not great, but it works

    setHeightFull();
    getStyle().set("display", "flex")
        .set("align-items", "center"); // center content vertically

    var currentUser = userSessionService.getUser();
    var avatar = new Avatar(currentUser.name(), currentUser.pictureUrl());
    avatar.getElement().getStyle().set("cursor", "pointer");

    var menu = createUserMenu(currentUser);
    menu.setTarget(avatar); // attach the menu to the avatar

    add(avatar);
  }

  private ContextMenu createUserMenu(OAuthUser currentUser) {
    var menu = new ContextMenu();
    menu.setOpenOnClick(true);

    var avatar = new Avatar(currentUser.name(), currentUser.pictureUrl());
    avatar.getStyle()
        .set("width", "60px")
        .set("height", "60px");

    var nameItem = new Span(currentUser.name());
    nameItem.getStyle()
        .set("font-size", "0.95em")
        .set("font-weight", "bold");

    var emailItem = new Span(currentUser.email());
    emailItem.getStyle()
        .set("font-size", "0.9em")
        .set("color", "lightgray");

    var userInfoLayout = new VerticalLayout(avatar, nameItem, emailItem);
    userInfoLayout.setAlignItems(CENTER);
    userInfoLayout.getStyle()
        .set("gap", "var(--lumo-space-xs)")
        .set("padding", "var(--lumo-space-s)");

    var logoutItem = new Div(new Span("Logout"));
    logoutItem.getStyle()
        .set("cursor", "pointer")
        .set("padding", "var(--lumo-space-xs)")
        .set("color", "var(--lumo-primary-text-color)");
    logoutItem.addClickListener(e -> userSessionService.logout());

    // wrap logout item in a layout to center it
    var logoutLayout = new VerticalLayout(logoutItem);
    logoutLayout.setPadding(false);
    logoutLayout.setAlignItems(CENTER);
    logoutLayout.setJustifyContentMode(JustifyContentMode.CENTER);

    menu.add(
        userInfoLayout,
        new Hr(),
        logoutLayout
    );

    return menu;
  }

  private void loadCurrentUser() {
    var currentUser = astUserService.getCurrentUser();
    applicationState.setCurrentUser(currentUser);
  }

}
