package com.dario.ast.view.component.folder;

import static com.dario.ast.util.EventUtil.folderUpdated;
import static org.springframework.util.StringUtils.hasText;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.Folder;
import com.dario.ast.core.service.FolderService;
import com.dario.ast.core.service.RequestService;
import com.dario.ast.event.folder.FocusFolderNameEvent;
import com.dario.ast.event.folder.FolderSelectedEvent;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UIScope
@SpringComponent
@CssImport(value = "./styles/folder-layout.css")
public class FolderLayout extends VerticalLayout {

  private final AppState appState;
  private final RequestService requestService;
  private final FolderService folderService;

  private final TextField nameText = new TextField();

  private boolean isUiLoading = false;
  private Folder selectedFolder;

  public FolderLayout(AppState appState, RequestService requestService, FolderService folderService) {
    this.appState = appState;
    this.requestService = requestService;
    this.folderService = folderService;

    setWidthFull();
    setSpacing(false);
    setHeight("fit-content");
    addClassNames("card-layout", "folder-layout");

    // name
    nameText.setPlaceholder("Folder name");
    nameText.setWidthFull();
    nameText.setMinWidth("0");
    nameText.getStyle().set("padding-top", "var(--lumo-space-m)");

    addListeners();

    add(
        new H4("Folder"),
        nameText
    );
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Listen for events indicating that a folder was selected (in RequestGrid)
    ComponentUtil.addListener(attachEvent.getUI(),
        FolderSelectedEvent.class,
        event -> loadFolderIntoUI(event.getFolder())
    );

    // Listen for events indicating that the folder name should be focused
    ComponentUtil.addListener(attachEvent.getUI(),
        FocusFolderNameEvent.class,
        event -> nameText.focus()
    );
  }

  private void addListeners() {
    // name
    nameText.addValueChangeListener(event -> {
      if (isUiLoading) {
        return;
      }

      var oldValue = event.getOldValue();
      var newValue = event.getValue();
      if (!hasText(newValue)) {
        nameText.setValue(oldValue);
        return;
      }
      if (!newValue.equals(oldValue)) {
        saveChanges();
      }
    });
  }

  private void saveChanges() {
    var folder = getFolderFromUi();

    try {
      folderService.save(folder);
    } catch (Exception ex) {
      ErrorNotification.show("Error saving folder");
      throw ex;
    }

    updateFolderInSelectedRequest(folder);
    folderUpdated(folder);
  }

  private void updateFolderInSelectedRequest(Folder folder) {
    if (appState.getSelectedRequest().getFolder().getId().equals(folder.getId())) {
      var selectedRequest = appState.getSelectedRequest();
      selectedRequest.setFolder(folder);
      appState.setSelectedRequest(selectedRequest);
    }
  }

  private Folder getFolderFromUi() {
    var name = nameText.getValue();

    return new Folder(
        selectedFolder.getId(),
        selectedFolder.getUserId(),
        name,
        selectedFolder.getCreatedOn()
    );
  }

  private void loadFolderIntoUI(Folder folder) {
    isUiLoading = true;

    selectedFolder = folder;
    nameText.setValue(folder.getName());

    isUiLoading = false;
    log.debug("Folder [{}] loaded into {}", folder.getName(), getClass().getSimpleName());
  }

}
