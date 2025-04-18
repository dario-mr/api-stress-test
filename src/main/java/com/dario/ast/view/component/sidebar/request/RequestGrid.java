package com.dario.ast.view.component.sidebar.request;

import static com.dario.ast.core.domain.RequestType.REQUEST;
import static com.dario.ast.util.EventUtil.focusRequestName;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.Folder;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.event.AstRequestCreatedEvent;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UIScope
@SpringComponent
public class RequestGrid extends TreeGrid<Object> {

  private final AstRequestService astRequestService;
  private final AppState appState;

  private TreeDataProvider<Object> dataProvider;

  public RequestGrid(AstRequestService astRequestService, AppState appState) {
    this.astRequestService = astRequestService;
    this.appState = appState;

    addClassName("sidebar-grid");

    // name column
    addComponentHierarchyColumn(item -> {
      if (item instanceof Folder folder) {
        return new Span(folder.getName());
      }
      if (item instanceof AstRequest request) {
        return new Span(request.getConfigParams().getRequestName());
      }
      return new Span("⚠️ Unknown row type [%s]".formatted(item.getClass().getSimpleName()));
    })
        .setAutoWidth(true)
        .setFlexGrow(1);

    // delete button
    addDeleteColumn();

    // click listener to load request into UI
    addItemClickListener(event -> {
      if (event.getItem() instanceof AstRequest request) {
        selectItem(request);
      } else if (event.getItem() instanceof Folder folder) {
        if (isExpanded(folder)) {
          collapse(folder);
        } else {
          expand(folder);
        }
      }
    });

    observeAppState();
    loadRequests();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Ensure the event is fired only after the UI is fully initialized
    getUI().ifPresent(ui -> ui.access(this::selectFirstItem));

    // Listen for events indicating that a new request was created
    ComponentUtil.addListener(attachEvent.getUI(),
        AstRequestCreatedEvent.class,
        event -> addAstRequest(event.getAstRequestId())
    );
  }

  private void observeAppState() {
    appState.getConfigParamsStream().subscribe(configParams -> {
          if (dataProvider != null) {
            dataProvider.refreshItem(new AstRequest(configParams, appState.getRunParams(), null)); // todo handle folder
          }
        }
    );

    appState.getRunParamsStream().subscribe(runParams -> {
          if (dataProvider != null) {
            dataProvider.refreshItem(new AstRequest(appState.getConfigParams(), runParams, null)); // todo handle folder
          }
        }
    );
  }

  private void addDeleteColumn() {
    addColumn(new ComponentRenderer<>(item -> {
      var deleteButton = new Button(TRASH.create(), e -> {
        if (item instanceof AstRequest request) {
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

    var treeData = new TreeData<>();
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

  private Map<Folder, List<AstRequest>> getUserRequestsByFolder(long currentUserId) {
    try {
      return astRequestService.getFoldersByUserIdAndTypeAndStatus(currentUserId, REQUEST, true);
    } catch (Exception ex) {
      log.error("Error fetching requests for user [{}]", currentUserId, ex);
      ErrorNotification.show("Error fetching requests");
      throw ex;
    }
  }

  private void selectFirstItem() {
    var rootItems = dataProvider.getTreeData().getRootItems();
    if (rootItems.isEmpty()) {
      return;
    }

    var firstItem = rootItems.getFirst();
    if (firstItem instanceof AstRequest request) {
      selectItem(request);
    } else if (firstItem instanceof Folder folder) {
      var children = dataProvider.getTreeData().getChildren(folder);
      if (!children.isEmpty() && children.getFirst() instanceof AstRequest request) {
        selectItem(request);
        expand(folder);
      }
    }
  }

  private void selectItem(AstRequest astRequest) {
    appState.setConfigParams(astRequest.getConfigParams());
    appState.setRunParams(astRequest.getRunParams());

    getSelectionModel().select(astRequest); // highlight item in the grid
  }

  private void addAstRequest(Long astRequestId) {
    var optAstRequest = astRequestService.getById(astRequestId);
    if (optAstRequest.isEmpty()) {
      return;
    }

    var newAstRequest = optAstRequest.get();

    dataProvider.getTreeData().addRootItems(newAstRequest);
    dataProvider.refreshAll();

    selectItem(newAstRequest); // set new request as currently selected item
    focusRequestName(); // focus the request name in Config layout
  }

  private void deleteRequestDialog(AstRequest request) {
    var deleteDialog = new ConfirmDialog();
    deleteDialog.setHeader("Delete request?");
    deleteDialog.setCancelable(true);
    deleteDialog.setConfirmText("Delete");
    deleteDialog.setCancelText("Cancel");
    deleteDialog.setConfirmButtonTheme("error primary");
    deleteDialog.addConfirmListener(event -> deleteAstRequest(request));

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

  private void deleteAstRequest(AstRequest toDelete) {
    try {
      astRequestService.delete(toDelete.getConfigParams().getRequestId());
    } catch (Exception ex) {
      log.error("Error deleting request {}", toDelete.getConfigParams().getRequestId(), ex);
      ErrorNotification.show("Error deleting request");
      return;
    }

    var selectedRequest = getSelectionModel().getFirstSelectedItem();

    dataProvider.getTreeData().removeItem(toDelete);
    dataProvider.refreshAll();
    
    // TODO fix: when deleted request was the last one of a folder, logic below does not work

    // if the deleted item was currently selected, select another one (first in data provider)
    if (selectedRequest.isPresent() && selectedRequest.get().equals(toDelete)) {
      selectFirstItem();
    }
  }

  private void deleteFolder(Folder toDelete) {
    try {
      astRequestService.deleteFolder(toDelete.getId());
    } catch (Exception ex) {
      log.error("Error deleting folder {}", toDelete, ex);
      ErrorNotification.show("Error deleting folder");
      return;
    }

    var selectedItem = getSelectionModel().getFirstSelectedItem();

    // Save requests of the deleted folder, to know if one of them was selected
    var treeData = dataProvider.getTreeData();
    var childrenRequests = treeData.getChildren(toDelete).stream()
        .filter(item -> item instanceof AstRequest)
        .map(item -> (AstRequest) item)
        .toList();

    // Remove the folder itself
    treeData.removeItem(toDelete);
    dataProvider.refreshAll();

    // If one of the folder requests was deleted, select another item
    if (selectedItem.isPresent() && selectedItem.get() instanceof AstRequest selectedRequest) {
      if (childrenRequests.contains(selectedRequest)) {
        selectFirstItem();
      }
    }
  }

}
