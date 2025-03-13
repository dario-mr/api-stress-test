package com.dario.ast.view.component.sidebar;

import static com.dario.ast.util.EventUtil.applyConfigParams;
import static com.dario.ast.util.EventUtil.applyRunParams;
import static com.dario.ast.util.EventUtil.focusRequestName;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.GoogleUser;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.core.service.UserSessionService;
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
  private final UserSessionService userSessionService;

  private ListDataProvider<AstRequest> dataProvider;

  @Autowired
  public RequestGrid(AstRequestService astRequestService, UserSessionService userSessionService) {
    this.astRequestService = astRequestService;
    this.userSessionService = userSessionService;

    addClassName("requests-grid");

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
        event -> updateAsrRequest(event.getAstRequest())
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

  private void loadRequests() {
    var currentUser = userSessionService.getUser();
    var userRequests = getUserRequests(currentUser);

    dataProvider = new ListDataProvider<>(userRequests);
    setDataProvider(dataProvider);
  }

  private ArrayList<AstRequest> getUserRequests(GoogleUser currentUser) {
    try {
      return new ArrayList<>(astRequestService.getByEmail(currentUser.email())); // mutable list
    } catch (Exception ex) {
      log.error("Error fetching [{}] requests", currentUser, ex);
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

  private void updateAsrRequest(AstRequest updatedRequest) {
    var optSelectedRequest = getSelectionModel().getFirstSelectedItem();
    if (optSelectedRequest.isEmpty()) {
      return;
    }

    var selectedRequest = optSelectedRequest.get();
    selectedRequest.setConfigParams(updatedRequest.getConfigParams());
    selectedRequest.setRunParams(updatedRequest.getRunParams());

    dataProvider.refreshItem(selectedRequest);
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
