package com.dario.ast.view.component.headline;

import com.dario.ast.core.domain.GoogleUser;
import com.dario.ast.core.service.UserSessionService;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class UserAvatar extends Div {

  private final UserSessionService userSessionService;

  public UserAvatar(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
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

  private ContextMenu createUserMenu(GoogleUser currentUser) {
    // TODO remove focus on the first item when opening the menu, cannot figure out how
    var menu = new ContextMenu();
    menu.setOpenOnClick(true);

    var avatar = new Avatar(currentUser.name(), currentUser.pictureUrl());
    avatar.getStyle()
        .set("width", "60px")
        .set("height", "60px");

    var nameItem = new Span(currentUser.name());
    nameItem.getStyle().set("font-weight", "bold");

    var emailItem = new Span(currentUser.email());
    emailItem.getStyle().set("font-size", "0.95em").set("color", "lightgray");

    var userInfoLayout = new VerticalLayout(avatar, nameItem, emailItem);
    userInfoLayout.setPadding(false);
    userInfoLayout.setAlignItems(CENTER);

    menu.addItem(userInfoLayout);
    menu.add(new Hr()); // separator
    menu.addItem("Logout", e -> userSessionService.logout());

    return menu;
  }
}
