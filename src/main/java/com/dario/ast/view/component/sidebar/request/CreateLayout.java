package com.dario.ast.view.component.sidebar.request;

import static com.dario.ast.core.domain.Folder.defaultFolder;
import static com.dario.ast.core.domain.RequestType.REQUEST;
import static com.dario.ast.core.domain.RunParams.defaultRunParams;
import static com.dario.ast.util.EventUtil.folderCreated;
import static com.dario.ast.util.EventUtil.requestCreated;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static org.springframework.http.HttpMethod.GET;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Request;
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

    var createRequestButton = new Button("\uD83D\uDCE8");
    createRequestButton.setTooltipText("Create request");
    createRequestButton.addClassName("create-button");
    createRequestButton.setHeightFull();
    createRequestButton.addClickListener(event -> createRequest());

    var createFolderButton = new Button("\uD83D\uDCC1");
    createFolderButton.setTooltipText("Create folder");
    createFolderButton.addClassName("create-button");
    createFolderButton.setHeightFull();
    createFolderButton.addClickListener(event -> createFolder());

    var buttons = new HorizontalLayout(createRequestButton, createFolderButton);
    buttons.setHeightFull();
    buttons.setSpacing(false);

    add(title, buttons);
  }

  // TODO if folder is selected, request should have it as parent
  private void createRequest() {
    var currentUserId = appState.getCurrentUser().getId();
    var configParams = defaultConfigParams(currentUserId);
    var runParams = defaultRunParams();
    var request = new Request(configParams, runParams, null);

    try {
      var requestId = requestService.create(request);
      requestCreated(requestId);
    } catch (Exception ex) {
      log.error("Error creating request", ex);
      ErrorNotification.show("Error creating request");
      throw ex;
    }
  }

  private static ConfigParams defaultConfigParams(Long userId) {
    return ConfigParams.builder()
        .requestName("New Request")
        .userId(userId)
        .uri("")
        .method(GET)
        .requestBody("")
        .requestType(REQUEST)
        .active(true)
        .build();
  }

  private void createFolder() {
    var currentUserId = appState.getCurrentUser().getId();
    var folder = defaultFolder(currentUserId);

    try {
      var newFolder = folderService.save(folder);
      folderCreated(newFolder);
    } catch (Exception ex) {
      log.error("Error creating folder", ex);
      ErrorNotification.show("Error creating folder");
      throw ex;
    }
  }

}