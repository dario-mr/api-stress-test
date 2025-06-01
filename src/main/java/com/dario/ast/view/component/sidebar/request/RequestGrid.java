package com.dario.ast.view.component.sidebar.request;

import static com.dario.ast.core.domain.RequestType.REQUEST;
import static com.dario.ast.util.EventUtil.focusFolderName;
import static com.dario.ast.util.EventUtil.focusRequestName;
import static com.dario.ast.util.EventUtil.folderSelected;
import static com.vaadin.flow.component.grid.dnd.GridDropMode.ON_TOP;
import static com.vaadin.flow.component.icon.VaadinIcon.COPY_O;
import static com.vaadin.flow.component.icon.VaadinIcon.FILE_ADD;
import static com.vaadin.flow.component.icon.VaadinIcon.FOLDER_ADD;
import static com.vaadin.flow.component.icon.VaadinIcon.FOLDER_OPEN_O;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.Folder;
import com.dario.ast.core.domain.Request;
import com.dario.ast.core.service.FolderService;
import com.dario.ast.core.service.RequestService;
import com.dario.ast.event.created.RequestCreatedEvent;
import com.dario.ast.event.folder.FolderCreatedEvent;
import com.dario.ast.event.folder.FolderUpdatedEvent;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.dario.ast.view.component.common.reactive.ReactiveComponent;
import com.dario.ast.view.component.common.reactive.ReactiveHandler;
import com.dario.ast.view.component.common.reactive.ReactiveType;
import com.dario.ast.view.component.folder.FolderLayout;
import com.dario.ast.view.component.request.RequestLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UIScope
@SpringComponent
@ReactiveComponent
@CssImport(value = "./styles/grid-tree-toggle-adjust.css", themeFor = "vaadin-grid-tree-toggle")
@CssImport(value = "./styles/request-grid.css", themeFor = "vaadin-grid")
public class RequestGrid extends TreeGrid<RequestOrFolder> {
  // TODO better icons

  private static final String GRID_BUTTON_CLASS = "grid-button";

  private final RequestService requestService;
  private final AppState appState;
  private final RequestLayout requestLayout;
  private final FolderLayout folderLayout;
  private final FolderService folderService;

  private TreeDataProvider<RequestOrFolder> dataProvider = new TreeDataProvider<>(new TreeData<>());
  private Request currentlyDraggedRequest;

  public RequestGrid(
      RequestService requestService,
      AppState appState,
      RequestLayout requestLayout,
      FolderLayout folderLayout,
      FolderService folderService) {
    this.requestService = requestService;
    this.appState = appState;
    this.requestLayout = requestLayout;
    this.folderLayout = folderLayout;
    this.folderService = folderService;

    configureGrid();
    registerListeners();
    loadRequests();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Listen for events indicating that a new request was created
    ComponentUtil.addListener(attachEvent.getUI(),
        RequestCreatedEvent.class,
        event -> addRequest(event.getRequest())
    );

    // Listen for events indicating that a folder was updated
    ComponentUtil.addListener(attachEvent.getUI(),
        FolderUpdatedEvent.class,
        event -> updateFolderInDataProvider(event.getFolder())
    );

    // Listen for events indicating that a folder was created
    ComponentUtil.addListener(attachEvent.getUI(),
        FolderCreatedEvent.class,
        event -> addFolder(event.getFolder())
    );
  }

  @ReactiveHandler(ReactiveType.REQUEST)
  public void onSelectedRequestChange(Request updatedRequest) {
    updateRequestInDataProvider(updatedRequest);
  }

  private void configureGrid() {
    addClassName("sidebar-grid");
    setRowsDraggable(true);
    setDropMode(ON_TOP);

    // name column
    addComponentHierarchyColumn(item -> {
      if (item instanceof Folder folder) {
        return buildFolderNameElement(folder);
      }
      if (item instanceof Request request) {
        return buildRequestNameElement(request);
      }

      return new Span("⚠️ Unknown row type [%s]".formatted(item.getClass().getSimpleName()));
    })
        .setFlexGrow(1)
        .setResizable(true);

    // create request button
    addCreateRequestColumn();

    // create folder button
    addCreateFolderColumn();

    // duplicate request button
    addDuplicateRequestColumn();

    // delete button
    addDeleteButtonColumn();
  }

  private static Span buildFolderNameElement(Folder folder) {
    var icon = new Icon(FOLDER_OPEN_O);
    var name = new Span(folder.getName());
    name.getStyle().set("margin-left", "var(--lumo-space-s)");

    return new Span(icon, name);
  }

  private static Span buildRequestNameElement(Request request) {
    var requestSpan = new Span(request.getConfigParams().getRequestName());
    if (request.getFolder() != null) {
      requestSpan.addClassName("request-in-folder");
    }

    return requestSpan;
  }

  private void registerListeners() {
    // allow only single selection
    asSingleSelect().addValueChangeListener(event -> {
      if (event.getValue() == null && event.getOldValue() != null) {
        asSingleSelect().setValue(event.getOldValue());
      }
    });

    addItemClickListener(this::handleItemClick);
    addDragStartListener(this::handleDragStart);
    addDragEndListener(event -> currentlyDraggedRequest = null);
    addDropListener(this::handleDropEvent);
  }

  private void updateRequestInDataProvider(Request updatedRequest) {
    var treeData = dataProvider.getTreeData();

    var potentialParents = new ArrayList<>(treeData.getRootItems());
    potentialParents.add(null); // root-level

    for (var parent : potentialParents) {
      for (var child : treeData.getChildren(parent)) {
        if (child instanceof Request existingRequest && existingRequest.equals(updatedRequest)) {
          existingRequest.updateFrom(updatedRequest);
          dataProvider.refreshItem(existingRequest);
          return;
        }
      }
    }
  }

  private void updateFolderInDataProvider(Folder updatedFolder) {
    var treeData = dataProvider.getTreeData();

    // find the parent (null if it's a root item)
    var parent = treeData.getRootItems().stream()
        .filter(item -> item instanceof Folder folder && folder.getId().equals(updatedFolder.getId()))
        .findAny()
        .map(treeData::getParent)
        .orElse(null);

    // find the original item by ID
    var existingFolderOpt = treeData.getChildren(parent).stream()
        .filter(item -> item instanceof Folder folder && folder.getId().equals(updatedFolder.getId()))
        .findFirst();

    if (existingFolderOpt.isPresent()) {
      var existingFolder = (Folder) existingFolderOpt.get();
      existingFolder.updateFrom(updatedFolder);
      dataProvider.refreshItem(existingFolder);
    }
  }

  private void addCreateRequestColumn() {
    addColumn(new ComponentRenderer<>(item -> {
      if (item instanceof Folder folder) {
        var createButton = new Button(FILE_ADD.create(), e -> createRequest(folder));
        createButton.setTooltipText("Create request");
        createButton.addClassName(GRID_BUTTON_CLASS);
        return createButton;
      }

      return noActionButton();
    }))
        .setAutoWidth(true)
        .setFlexGrow(0)
        .setClassNameGenerator(item -> !(item instanceof Folder) ? "empty-cell" : "");
  }

  private void addCreateFolderColumn() {
    addColumn(new ComponentRenderer<>(item -> {
      if (item instanceof Folder parentFolder) {
        var createButton = new Button(FOLDER_ADD.create(), e -> createFolder(parentFolder));
        createButton.setTooltipText("Create folder");
        createButton.addClassName(GRID_BUTTON_CLASS);
        return createButton;
      }

      return noActionButton();
    }))
        .setAutoWidth(true)
        .setFlexGrow(0)
        .setClassNameGenerator(item -> !(item instanceof Folder) ? "empty-cell" : "");
  }


  private void createFolder(Folder parentFolder) {
    var currentUserId = appState.getCurrentUser().getId();

    try {
      folderService.createFolder(currentUserId, parentFolder);
    } catch (Exception ex) {
      log.error("Error creating folder", ex);
      ErrorNotification.show("Error creating folder");
      throw ex;
    }
  }

  private void createRequest(Folder parentFolder) {
    var currentUserId = appState.getCurrentUser().getId();

    try {
      requestService.createDefaultRequestAndNotify(currentUserId, parentFolder);
    } catch (Exception ex) {
      log.error("Error creating request", ex);
      ErrorNotification.show("Error creating request");
      throw ex;
    }
  }

  private void addDuplicateRequestColumn() {
    addColumn(new ComponentRenderer<>(item -> {
      if (item instanceof Request request) {
        var duplicateButton = new Button(COPY_O.create(), e -> duplicateRequest(request));
        duplicateButton.setTooltipText("Duplicate request");
        duplicateButton.addClassName(GRID_BUTTON_CLASS);
        return duplicateButton;
      }

      return noActionButton();
    }))
        .setAutoWidth(true)
        .setFlexGrow(0)
        .setClassNameGenerator(item -> !(item instanceof Request) ? "empty-cell" : "");
  }

  private void duplicateRequest(Request request) {
    try {
      requestService.duplicateRequestAndNotify(request);
    } catch (Exception ex) {
      log.error("Error duplicating request", ex);
      ErrorNotification.show("Error duplicating request");
      throw ex;
    }
  }

  private void addDeleteButtonColumn() {
    addColumn(new ComponentRenderer<>(item -> {
      var deleteButton = new Button(TRASH.create(), e -> {
        if (item instanceof Request request) {
          showDeleteDialog("Delete request?", () -> deleteRequest(request));
        } else if (item instanceof Folder folder) {
          showDeleteDialog("Delete folder?", () -> deleteFolder(folder));
        }
      });

      deleteButton.addClassName(GRID_BUTTON_CLASS);
      deleteButton.setTooltipText("Delete item");
      return deleteButton;
    }))
        .setAutoWidth(true)
        .setFlexGrow(0);
  }

  // TODO redesign to support nested folders
  private void loadRequests() {
    var currentUserId = appState.getCurrentUser().getId();
    var userRequestsByFolder = getUserRequestsByFolder(currentUserId);

    var treeData = new TreeData<RequestOrFolder>();
    userRequestsByFolder.forEach((folder, requests) -> {
      if (folder == null) { // root level request
        requests.forEach(request -> treeData.addItem(null, request));
      } else {
        treeData.addItem(null, folder);
        requests.forEach(request -> treeData.addItem(folder, request));
      }
    });

    dataProvider = new TreeDataProvider<>(treeData);
    setDataProvider(dataProvider);

    selectFirstRequest();
  }

  private Map<Folder, List<Request>> getUserRequestsByFolder(long currentUserId) {
    try {
      return requestService.getFoldersByUserIdAndTypeAndStatus(currentUserId, REQUEST, true);
    } catch (Exception ex) {
      log.error("Error fetching requests for user [{}]", currentUserId, ex);
      ErrorNotification.show("Error fetching requests");
      throw ex;
    }
  }

  private void selectFirstRequest() {
    for (var rootItem : dataProvider.getTreeData().getRootItems()) {
      if (rootItem instanceof Request request) {
        selectRequest(request);
        return;
      } else if (rootItem instanceof Folder folder) {
        var firstRequest = dataProvider.getTreeData().getChildren(folder).stream()
            .filter(Request.class::isInstance)
            .map(Request.class::cast)
            .findFirst();

        if (firstRequest.isPresent()) {
          expand(folder);
          selectRequest(firstRequest.get());
          return;
        }
      }
    }
  }

  private void selectRequest(Request request) {
    appState.setSelectedRequest(request);
    getSelectionModel().select(request); // highlight item in the grid
  }

  private void selectFolder(Folder folder) {
    getSelectionModel().select(folder); // highlight item in the grid
    folderSelected(folder); // load folder into Folder Layout
    focusFolderName(); // focus the folder name in Folder layout
  }

  private void addRequest(Request newRequest) {
    dataProvider.getTreeData().addItem(newRequest.getFolder(), newRequest);
    dataProvider.refreshAll();

    requestLayout.setVisible(true);
    folderLayout.setVisible(false);

    if (newRequest.getFolder() != null) {
      expand(newRequest.getFolder());
    }

    selectRequest(newRequest); // set new request as currently selected item
    focusRequestName(); // focus the request name in Config layout
  }

  private void addFolder(Folder folder) {
    dataProvider.getTreeData().addRootItems(folder);
    dataProvider.refreshAll();

    requestLayout.setVisible(false);
    folderLayout.setVisible(true);

    selectFolder(folder);
  }

  private void showDeleteDialog(String title, Runnable onConfirm) {
    var dialog = new ConfirmDialog();
    dialog.setHeader(title);
    dialog.setCancelable(true);
    dialog.setConfirmText("Delete");
    dialog.setCancelText("Cancel");
    dialog.setConfirmButtonTheme("error primary");
    dialog.addConfirmListener(e -> onConfirm.run());
    dialog.open();
  }

  private void deleteRequest(Request toDelete) {
    try {
      requestService.delete(toDelete.getConfigParams().getRequestId());
    } catch (Exception ex) {
      log.error("Error deleting request {}", toDelete.getConfigParams().getRequestId(), ex);
      ErrorNotification.show("Error deleting request");
      return;
    }

    var selectedRequest = getSelectionModel().getFirstSelectedItem();

    dataProvider.getTreeData().removeItem(toDelete);
    dataProvider.refreshAll();

    // if the deleted item was currently selected, select another one (first in data provider)
    if (selectedRequest.isPresent() && selectedRequest.get().equals(toDelete)) {
      selectFirstRequest();
    }
  }

  private void deleteFolder(Folder toDelete) {
    try {
      folderService.delete(toDelete.getId());
    } catch (Exception ex) {
      log.error("Error deleting folder {}", toDelete, ex);
      ErrorNotification.show("Error deleting folder");
      return;
    }

    var selectedItem = getSelectionModel().getFirstSelectedItem();

    // Save requests of the deleted folder, to know if one of them was selected
    var treeData = dataProvider.getTreeData();
    var childrenRequests = treeData.getChildren(toDelete).stream()
        .filter(item -> item instanceof Request)
        .map(item -> (Request) item)
        .toList();

    // Remove the folder itself
    treeData.removeItem(toDelete);
    dataProvider.refreshAll();

    // If one of the folder requests was deleted, select another item
    if (selectedItem.isPresent() && selectedItem.get() instanceof Request selectedRequest) {
      if (childrenRequests.contains(selectedRequest)) {
        selectFirstRequest();
      }
    }
  }

  private void handleItemClick(ItemClickEvent<RequestOrFolder> event) {
    requestLayout.setVisible(false);
    folderLayout.setVisible(false);

    if (event.getItem() instanceof Request request) {
      requestLayout.setVisible(true);
      selectRequest(request);
    } else if (event.getItem() instanceof Folder folder) {
      folderLayout.setVisible(true);
      folderSelected(folder);
    }
  }

  private void handleDragStart(GridDragStartEvent<RequestOrFolder> event) {
    // only allow dragging requests
    var draggedItem = event.getDraggedItems().stream().findFirst().orElse(null);
    if (draggedItem instanceof Request request) {
      currentlyDraggedRequest = request;
    } else {
      currentlyDraggedRequest = null;
    }
  }

  private void handleDropEvent(GridDropEvent<RequestOrFolder> event) {
    if (currentlyDraggedRequest == null) {
      return;
    }

    var dropTarget = event.getDropTargetItem().orElse(null);

    if (dropTarget instanceof Folder targetFolder) {
      moveRequestToFolder(currentlyDraggedRequest, targetFolder);
    }
  }

  private void moveRequestToFolder(Request request, Folder newFolder) {
    try {
      // Update in backend
      requestService.updateFolder(request.getConfigParams().getRequestId(), newFolder);

      // Update UI
      var treeData = dataProvider.getTreeData();
      treeData.removeItem(request);
      request.setFolder(newFolder);
      treeData.addItem(newFolder, request);

      dataProvider.refreshAll();

      expand(newFolder);
      selectRequest(request);
    } catch (Exception ex) {
      log.error("Failed to move request to folder", ex);
      ErrorNotification.show("Failed to move request to folder");
    }
  }

  private static Button noActionButton() {
    var noAction = new Button();
    noAction.setVisible(false);
    return noAction;
  }

}
