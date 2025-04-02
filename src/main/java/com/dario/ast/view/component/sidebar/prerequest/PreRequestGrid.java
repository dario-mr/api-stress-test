package com.dario.ast.view.component.sidebar.prerequest;

import static com.dario.ast.core.domain.RequestType.PRE_REQUEST;
import static com.dario.ast.util.EventUtil.applyPreRequest;
import static com.dario.ast.util.EventUtil.focusPreRequestName;
import static com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.event.PreRequestCreatedEvent;
import com.dario.ast.event.PreRequestUpdatedEvent;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UIScope
@SpringComponent
public class PreRequestGrid extends Grid<AstRequest> {

  private final AstRequestService astRequestService;
  private final AppState appState;

  private ListDataProvider<AstRequest> dataProvider;
  private AstRequest lastSelectedItem;

  public PreRequestGrid(AstRequestService astRequestService, AppState appState) {
    this.astRequestService = astRequestService;
    this.appState = appState;

    addClassName("sidebar-grid");

    // name column
    addColumn(preRequest -> preRequest.getConfigParams().getRequestName())
        .setHeader("Name")
        .setAutoWidth(true)
        .setFlexGrow(1);

    // active column
    addColumn(new ComponentRenderer<>(preRequest -> {
      var checkbox = new Checkbox(preRequest.getConfigParams().isActive());
      checkbox.addValueChangeListener(event -> {
        preRequest.getConfigParams().setActive(event.getValue());
        astRequestService.update(preRequest);
        dataProvider.refreshItem(preRequest);
      });
      return checkbox;
    })).setHeader("Active")
        .setAutoWidth(true)
        .setFlexGrow(1);

    // delete button
    addDeleteColumn();

    // click listener to load request into UI
    addItemClickListener(event -> {
      var astRequest = event.getItem();
      selectItem(astRequest);
    });

    preventUnselection();
    loadRequests();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Ensure the event is fired only after the UI is fully initialized
    getUI().ifPresent(ui -> ui.access(this::selectFirstItem));

    // Listen for events indicating that a new pre-request was created
    ComponentUtil.addListener(attachEvent.getUI(),
        PreRequestCreatedEvent.class,
        event -> addPreRequest(event.getPreRequestId())
    );

    // Listen for events indicating that a pre-request was updated
    ComponentUtil.addListener(attachEvent.getUI(),
        PreRequestUpdatedEvent.class,
        event -> dataProvider.refreshItem(event.getPreRequest())
    );
  }

  private void addDeleteColumn() {
    addColumn(new ComponentRenderer<>(astRequest -> {
      var deleteButton = new Button(TRASH.create(), e -> showDeleteDialog(astRequest));
      deleteButton.addClassName("delete-button");
      return deleteButton;
    }))
        .setAutoWidth(true)
        .setFlexGrow(0);
  }

  private void preventUnselection() {
    // prevent unselection by restoring last selected item
    var selectionModel = setSelectionMode(SINGLE);
    selectionModel.addSelectionListener(event -> {
      if (event.getFirstSelectedItem().isEmpty() && lastSelectedItem != null) {
        selectionModel.select(lastSelectedItem);
      } else {
        lastSelectedItem = event.getFirstSelectedItem().orElse(null);
      }
    });
  }

  private void loadRequests() {
    var currentUserId = appState.getCurrentUser().getId();
    var userRequests = getUserRequests(currentUserId);

    dataProvider = new ListDataProvider<>(userRequests);
    setDataProvider(dataProvider);
  }

  private ArrayList<AstRequest> getUserRequests(long currentUserId) {
    try {
      return new ArrayList<>(astRequestService.getByUserIdAndType(currentUserId, PRE_REQUEST));
    } catch (Exception ex) {
      log.error("Error fetching pre-requests for user [{}]", currentUserId, ex);
      ErrorNotification.show("Error fetching requests");
      throw ex;
    }
  }

  private void selectFirstItem() {
    var astRequests = (List<AstRequest>) dataProvider.getItems();
    if (astRequests.isEmpty()) {
      return;
    }

    var firstRequest = astRequests.getFirst();
    selectItem(firstRequest);
  }

  private void selectItem(AstRequest preRequest) {
    applyPreRequest(preRequest);
    getSelectionModel().select(preRequest); // highlight item in the grid
  }

  private void addPreRequest(Long preRequestId) {
    var optPreRequest = astRequestService.getById(preRequestId);
    if (optPreRequest.isEmpty()) {
      return;
    }

    var newPreRequest = optPreRequest.get();

    dataProvider.getItems().add(newPreRequest);
    dataProvider.refreshAll();
    selectItem(newPreRequest); // set new request as currently selected item
    focusPreRequestName();
  }

  private void showDeleteDialog(AstRequest preRequest) {
    var deleteDialog = new ConfirmDialog();
    deleteDialog.setHeader("Delete request?");
    deleteDialog.setCancelable(true);
    deleteDialog.setConfirmText("Delete");
    deleteDialog.setCancelText("Cancel");
    deleteDialog.setConfirmButtonTheme("error primary");
    deleteDialog.addConfirmListener(event -> deletePreRequest(preRequest));

    deleteDialog.open();
  }

  private void deletePreRequest(AstRequest toDelete) {
    try {
      astRequestService.delete(toDelete.getConfigParams().getRequestId());
    } catch (Exception ex) {
      log.error("Error deleting pre-request {}", toDelete.getConfigParams().getRequestId(), ex);
      ErrorNotification.show("Error deleting request");
      return;
    }

    var selectedRequest = getSelectionModel().getFirstSelectedItem();

    // update data provider
    dataProvider.getItems().remove(toDelete);
    dataProvider.refreshAll();

    // if the deleted item was currently selected, select another one (first in data provider)
    if (selectedRequest.isPresent() && selectedRequest.get().equals(toDelete)) {
      selectFirstItem();
    }
  }

}
