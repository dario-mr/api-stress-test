package com.dario.ast.view.component.sidebar.request;

import static com.dario.ast.core.domain.RequestType.REQUEST;
import static com.dario.ast.util.EventUtil.focusFolderName;
import static com.dario.ast.util.EventUtil.focusRequestName;
import static com.dario.ast.util.EventUtil.folderSelected;
import static com.vaadin.flow.component.grid.dnd.GridDropMode.ON_TOP;
import static com.vaadin.flow.component.icon.VaadinIcon.FILE_ADD;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.Folder;
import com.dario.ast.core.domain.Request;
import com.dario.ast.core.service.RequestService;
import com.dario.ast.event.RequestCreatedEvent;
import com.dario.ast.event.folder.FolderCreatedEvent;
import com.dario.ast.event.folder.FolderUpdatedEvent;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.dario.ast.view.component.common.reactive.ReactiveBinderSupport;
import com.dario.ast.view.component.common.reactive.ReactiveSubscription;
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
import reactor.core.publisher.Flux;

@Slf4j
@UIScope
@SpringComponent
@CssImport(value = "./styles/grid-tree-toggle-adjust.css", themeFor = "vaadin-grid-tree-toggle")
public class RequestGrid extends TreeGrid<RequestOrFolder> implements ReactiveBinderSupport {

  // TODO general refactor

  private final RequestService requestService;
  private final AppState appState;
  private final RequestLayout requestLayout;
  private final FolderLayout folderLayout;

  private TreeDataProvider<RequestOrFolder> dataProvider;
  private Request currentlyDraggedRequest;

  public RequestGrid(
      RequestService requestService,
      AppState appState,
      RequestLayout requestLayout,
      FolderLayout folderLayout
  ) {
    this.requestService = requestService;
    this.appState = appState;
    this.requestLayout = requestLayout;
    this.folderLayout = folderLayout;

    addClassName("sidebar-grid");
    setRowsDraggable(true);
    setDropMode(ON_TOP);

    // name column
    addComponentHierarchyColumn(item -> {
      if (item instanceof Folder folder) {
        return new Span(folder.getName());
      }
      if (item instanceof Request request) {
        return new Span(request.getConfigParams().getRequestName());
      }
      return new Span("⚠️ Unknown row type [%s]".formatted(item.getClass().getSimpleName()));
    })
        .setAutoWidth(true)
        .setFlexGrow(1);

    // create request button
    addCreateRequestColumn();

    // delete button
    addDeleteColumn();

    // allow only single selection
    asSingleSelect().addValueChangeListener(event -> {
      if (event.getValue() == null && event.getOldValue() != null) {
        asSingleSelect().setValue(event.getOldValue());
      }
    });

    // listeners
    addItemClickListener(this::handleItemClick);
    addDragStartListener(this::handleDragStart);
    addDragEndListener(event -> currentlyDraggedRequest = null);
    addDropListener(this::handleDrop);

    loadRequests();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    getUI().ifPresent(ui -> bindReactiveSubscriptions(this, ui));

    // Ensure the event is fired only after the UI is fully initialized
    getUI().ifPresent(ui -> {
      if (ui.isAttached()) {
        ui.access(this::selectFirstItem);
      } else {
        log.warn("onAttach -> selectFirstItem: UI is not attached!");
      }
    });

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

  @ReactiveSubscription
  public Flux<Request> onRequestChange() {
    return appState.getSelectedRequestStream();
  }

  public void handle(Request updatedRequest) {
    updateRequestInDataProvider(updatedRequest);
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
        // TODO better icon
        var createButton = new Button(FILE_ADD.create(), e -> createRequest(folder));
        createButton.addClassName("delete-button");
        return createButton;
      }

      var noAction = new Button();
      noAction.setVisible(false);
      return noAction;
    }))
        .setAutoWidth(true)
        .setFlexGrow(0);
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

  private void addDeleteColumn() {
    addColumn(new ComponentRenderer<>(item -> {
      var deleteButton = new Button(TRASH.create(), e -> {
        if (item instanceof Request request) {
          deleteRequestDialog(request);
        } else if (item instanceof Folder folder) {
          deleteFolderDialog(folder);
        }
      });
      deleteButton.addClassName("delete-button");
      return deleteButton;
    }))
        .setAutoWidth(true)
        .setFlexGrow(0);
  }

  private void loadRequests() {
    var currentUserId = appState.getCurrentUser().getId();
    var userRequestsByFolder = getUserRequestsByFolder(currentUserId);

    var treeData = new TreeData<RequestOrFolder>();
    userRequestsByFolder.forEach((folder, requests) -> {
      if (folder == null) {
        requests.forEach(request -> treeData.addItem(null, request));
      } else {
        treeData.addItem(null, folder);
        requests.forEach(request -> treeData.addItem(folder, request));
      }
    });

    dataProvider = new TreeDataProvider<>(treeData);
    setDataProvider(dataProvider);
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

  private void selectFirstItem() {
    for (Object rootItem : dataProvider.getTreeData().getRootItems()) {
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

  private void deleteRequestDialog(Request request) {
    var deleteDialog = new ConfirmDialog();
    deleteDialog.setHeader("Delete request?");
    deleteDialog.setCancelable(true);
    deleteDialog.setConfirmText("Delete");
    deleteDialog.setCancelText("Cancel");
    deleteDialog.setConfirmButtonTheme("error primary");
    deleteDialog.addConfirmListener(event -> deleteRequest(request));

    deleteDialog.open();
  }

  private void deleteFolderDialog(Folder folder) {
    var deleteDialog = new ConfirmDialog();
    deleteDialog.setHeader("Delete folder?");
    deleteDialog.setCancelable(true);
    deleteDialog.setConfirmText("Delete");
    deleteDialog.setCancelText("Cancel");
    deleteDialog.setConfirmButtonTheme("error primary");
    deleteDialog.addConfirmListener(event -> deleteFolder(folder));

    deleteDialog.open();
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
      selectFirstItem();
    }
  }

  private void deleteFolder(Folder toDelete) {
    try {
      requestService.deleteFolder(toDelete.getId());
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
        selectFirstItem();
      }
    }
  }

  private void handleItemClick(ItemClickEvent<RequestOrFolder> event) {
    if (event.getItem() instanceof Request request) {
      requestLayout.setVisible(true);
      folderLayout.setVisible(false);
      selectRequest(request);
    } else if (event.getItem() instanceof Folder folder) {
      requestLayout.setVisible(false);
      folderLayout.setVisible(true);
      folderSelected(folder);
    }
  }

  private void handleDragStart(GridDragStartEvent<RequestOrFolder> event) {
    var draggedItem = event.getDraggedItems().stream().findFirst().orElse(null);
    if (draggedItem instanceof Request request) {
      currentlyDraggedRequest = request;
    } else {
      currentlyDraggedRequest = null;
    }
  }

  private void handleDrop(GridDropEvent<RequestOrFolder> event) {
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

}
