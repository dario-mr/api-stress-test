package com.dario.ast.view.component.sidebar.request;

import static com.vaadin.flow.component.icon.VaadinIcon.FILE_ADD;
import static com.vaadin.flow.component.icon.VaadinIcon.FOLDER_ADD;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.service.FolderService;
import com.dario.ast.core.service.RequestService;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UIScope
@SpringComponent
public class CreateLayout extends HorizontalLayout {

  private final RequestService requestService;
  private final FolderService folderService;
  private final AppState appState;

  public CreateLayout(RequestService requestService, FolderService folderService, AppState appState) {
    this.requestService = requestService;
    this.folderService = folderService;
    this.appState = appState;

    setWidthFull();
    setSpacing(false);
    setAlignItems(CENTER);
    setJustifyContentMode(JustifyContentMode.BETWEEN); // Title left, buttons right

    var title = new H4("Requests");
    title.getStyle().set("padding", "var(--lumo-space-m)");

    // TODO better icons
    var createRequestButton = new Button(FILE_ADD.create());
    createRequestButton.setTooltipText("Create request");
    createRequestButton.addClassName("create-button");
    createRequestButton.setHeightFull();
    createRequestButton.addClickListener(event -> createRequest());

    var createFolderButton = new Button(FOLDER_ADD.create());
    createFolderButton.setTooltipText("Create folder");
    createFolderButton.addClassName("create-button");
    createFolderButton.setHeightFull();
    createFolderButton.addClickListener(event -> createFolder());

    var buttons = new HorizontalLayout(createRequestButton, createFolderButton);
    buttons.setHeightFull();
    buttons.setSpacing(false);

    add(title, buttons);
  }

  private void createRequest() {
    var currentUserId = appState.getCurrentUser().getId();

    try {
      requestService.createDefaultRequestAndNotify(currentUserId, null);
    } catch (Exception ex) {
      log.error("Error creating request", ex);
      ErrorNotification.show("Error creating request");
      throw ex;
    }
  }

  private void createFolder() {
    var currentUserId = appState.getCurrentUser().getId();

    try {
      folderService.createFolder(currentUserId, null);
    } catch (Exception ex) {
      log.error("Error creating folder", ex);
      ErrorNotification.show("Error creating folder");
      throw ex;
    }
  }

}