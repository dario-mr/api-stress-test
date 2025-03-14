package com.dario.ast.view.component.sidebar.requests;

import static com.dario.ast.util.EventUtil.applyConfigParams;
import static com.dario.ast.util.EventUtil.applyRunParams;
import static com.dario.ast.util.EventUtil.focusRequestName;
import static com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.core.service.AstUserService;
import com.dario.ast.event.AsrRequestCreatedEvent;
import com.dario.ast.event.AsrRequestUpdatedEvent;
import com.dario.ast.view.component.notification.ErrorNotification;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@UIScope
@SpringComponent
public class RequestGrid extends Grid<AstRequest> {

  private final AstRequestService astRequestService;
  private final AstUserService astUserService;

  private ListDataProvider<AstRequest> dataProvider;
  private AstRequest lastSelectedItem;

  @Autowired
  public RequestGrid(AstRequestService astRequestService, AstUserService astUserService) {
    this.astRequestService = astRequestService;
    this.astUserService = astUserService;

    addClassName("sidebar-grid");

    // name column
    addColumn(astRequest -> astRequest.getConfigParams().getRequestName())
        .setAutoWidth(true)
        .setFlexGrow(1);

    // delete button
    addDeleteColumn();

    // click listener to load request into UI
    addItemClickListener(event -> {
      var astRequest = event.getItem();
      applyConfigParams(astRequest.getConfigParams());
      applyRunParams(astRequest.getRunParams());
    });

    preventUnselection();
    loadRequests();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Ensure the event is fired only after the UI is fully initialized
    getUI().ifPresent(ui -> ui.access(this::selectFirstItem));

    // Listen for events indicating that the selected AST request was updated
    ComponentUtil.addListener(attachEvent.getUI(),
        AsrRequestUpdatedEvent.class,
        event -> updateAsrRequestInDataProvider(event.getAstRequest())
    );

    // Listen for events indicating that a new AST request was created
    ComponentUtil.addListener(attachEvent.getUI(),
        AsrRequestCreatedEvent.class,
        event -> addAsrRequest(event.getAstRequestId())
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
    var currentUser = astUserService.getCurrentUser();
    var userRequests = getUserRequests(currentUser.getId());

    dataProvider = new ListDataProvider<>(userRequests);
    setDataProvider(dataProvider);
  }

  private ArrayList<AstRequest> getUserRequests(long currentUserId) {
    try {
      return new ArrayList<>(astRequestService.getByUserId(currentUserId)); // mutable list
    } catch (Exception ex) {
      log.error("Error fetching requests for user [{}]", currentUserId, ex);
      ErrorNotification.show("Error fetching requests");
      throw new RuntimeException(ex);
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

  private void selectItem(AstRequest astRequest) {
    applyConfigParams(astRequest.getConfigParams());
    applyRunParams(astRequest.getRunParams());
    getSelectionModel().select(astRequest); // highlight item in the grid
  }

  // if a request is updated by some other component, we need to refresh it in the grid data provider
  private void updateAsrRequestInDataProvider(AstRequest updatedRequest) {
    // find the updated request in the data provider (by requestId)
    var currentRequestOpt = dataProvider.getItems().stream()
        .filter(astRequest ->
            astRequest.getConfigParams().getRequestId().equals(updatedRequest.getConfigParams().getRequestId()))
        .findFirst();
    if (currentRequestOpt.isEmpty()) {
      return;
    }

    var currentRequest = currentRequestOpt.get();
    currentRequest.setConfigParams(updatedRequest.getConfigParams());
    currentRequest.setRunParams(updatedRequest.getRunParams());

    dataProvider.refreshItem(currentRequest);
  }

  private void addAsrRequest(Long astRequestId) {
    var optAsrRequest = astRequestService.getById(astRequestId);
    if (optAsrRequest.isEmpty()) {
      return;
    }

    var newAsrRequest = optAsrRequest.get();

    dataProvider.getItems().add(newAsrRequest);
    dataProvider.refreshAll();
    selectItem(newAsrRequest); // set new request as currently selected item
    focusRequestName(); // focus the request name in Config layout
  }

  private void showDeleteDialog(AstRequest astRequest) {
    var deleteDialog = new ConfirmDialog();
    deleteDialog.setHeader("Delete request?");
    deleteDialog.setCancelable(true);
    deleteDialog.setConfirmText("Delete");
    deleteDialog.setCancelText("Cancel");
    deleteDialog.setConfirmButtonTheme("error primary");
    deleteDialog.addConfirmListener(event -> deleteAstRequest(astRequest));

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

    // update data provider
    dataProvider.getItems().remove(toDelete);
    dataProvider.refreshAll();

    // if the deleted item was currently selected, select another one (first in data provider)
    if (selectedRequest.isPresent() && selectedRequest.get().equals(toDelete)) {
      selectFirstItem();
    }
  }

}
