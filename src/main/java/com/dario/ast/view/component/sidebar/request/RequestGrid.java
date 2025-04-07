package com.dario.ast.view.component.sidebar.request;

import static com.dario.ast.core.domain.RequestType.REQUEST;
import static com.dario.ast.util.EventUtil.focusRequestName;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.event.AstRequestCreatedEvent;
import com.dario.ast.view.component.common.notification.ErrorNotification;
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

@Slf4j
@UIScope
@SpringComponent
public class RequestGrid extends Grid<AstRequest> {

  private final AstRequestService astRequestService;
  private final AppState appState;

  private ListDataProvider<AstRequest> dataProvider;

  public RequestGrid(AstRequestService astRequestService, AppState appState) {
    this.astRequestService = astRequestService;
    this.appState = appState;

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
      selectItem(astRequest);
    });

    observeAppState();
    loadRequests();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Ensure the event is fired only after the UI is fully initialized
    getUI().ifPresent(ui -> ui.access(this::selectFirstItem));

    // Listen for events indicating that a new AST request was created
    ComponentUtil.addListener(attachEvent.getUI(),
        AstRequestCreatedEvent.class,
        event -> addAstRequest(event.getAstRequestId())
    );
  }

  private void observeAppState() {
    appState.getConfigParamsStream().subscribe(configParams ->
        dataProvider.refreshItem(new AstRequest(configParams, appState.getRunParams()))
    );

    appState.getRunParamsStream().subscribe(runParams ->
        dataProvider.refreshItem(new AstRequest(appState.getConfigParams(), runParams))
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

  private void loadRequests() {
    var currentUserId = appState.getCurrentUser().getId();
    var userRequests = getUserRequests(currentUserId);

    dataProvider = new ListDataProvider<>(userRequests);
    setDataProvider(dataProvider);
  }

  private ArrayList<AstRequest> getUserRequests(long currentUserId) {
    try {
      return new ArrayList<>(astRequestService.getByUserIdAndTypeAndStatus(currentUserId, REQUEST, true));
    } catch (Exception ex) {
      log.error("Error fetching requests for user [{}]", currentUserId, ex);
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

    dataProvider.getItems().add(newAstRequest);
    dataProvider.refreshAll();

    selectItem(newAstRequest); // set new request as currently selected item
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
