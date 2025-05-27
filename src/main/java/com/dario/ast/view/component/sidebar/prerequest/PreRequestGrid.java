package com.dario.ast.view.component.sidebar.prerequest;

import static com.dario.ast.core.domain.RequestType.PRE_REQUEST;
import static com.dario.ast.util.EventUtil.applyPreRequest;
import static com.dario.ast.util.EventUtil.focusPreRequestName;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Request;
import com.dario.ast.core.service.PreRequestService;
import com.dario.ast.core.service.RequestService;
import com.dario.ast.event.created.PreRequestCreatedEvent;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.dario.ast.view.component.common.reactive.ReactiveComponent;
import com.dario.ast.view.component.common.reactive.ReactiveHandler;
import com.dario.ast.view.component.common.reactive.ReactiveType;
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
@ReactiveComponent
public class PreRequestGrid extends Grid<ConfigParams> {

  private final RequestService requestService;
  private final AppState appState;

  private ListDataProvider<ConfigParams> dataProvider;
  private ConfigParams selectedPreRequestParams;
  private boolean firstLoad = true;

  public PreRequestGrid(RequestService requestService, AppState appState, PreRequestService preRequestService) {
    this.requestService = requestService;
    this.appState = appState;

    addClassName("sidebar-grid");

    // name column
    addColumn(ConfigParams::getRequestName)
        .setAutoWidth(true)
        .setFlexGrow(1);

    // active column
    addColumn(new ComponentRenderer<>(configParams -> {
      var checkbox = new Checkbox(configParams.isActive());
      checkbox.addClassName("active-column");
      checkbox.addValueChangeListener(event -> {
        configParams.setActive(event.getValue());
        requestService.updateConfigParams(configParams);
        preRequestService.updatePreRequestInAppState(configParams);
      });
      return checkbox;
    }))
        .setAutoWidth(true)
        .setFlexGrow(0);

    // delete button
    addDeleteColumn();

    // click listener to load request into UI
    addItemClickListener(event -> {
      var preRequestParams = event.getItem();
      selectItem(preRequestParams);
    });

    loadRequests();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Listen for events indicating that a new pre-request was created
    ComponentUtil.addListener(attachEvent.getUI(),
        PreRequestCreatedEvent.class,
        event -> addPreRequest(event.getPreRequestId())
    );
  }

  @ReactiveHandler(ReactiveType.CONFIG_PARAMS_LIST)
  public void onPreRequestsChanged(List<ConfigParams> preRequestParams) {
    dataProvider = new ListDataProvider<>(preRequestParams);
    setDataProvider(dataProvider);

    if (selectedPreRequestParams != null) {
      getSelectionModel().select(selectedPreRequestParams);
    }
    if (firstLoad) {
      firstLoad = false;
      selectFirstItem();
    }
  }

  private void addDeleteColumn() {
    addColumn(new ComponentRenderer<>(preRequestParams -> {
      var deleteButton = new Button(TRASH.create(), e -> showDeleteDialog(preRequestParams));
      deleteButton.addClassName("delete-button");
      return deleteButton;
    }))
        .setAutoWidth(true)
        .setFlexGrow(0);
  }

  private void loadRequests() {
    var currentUserId = appState.getCurrentUser().getId();
    var preRequestsParams = getUserPreRequestsParams(currentUserId);

    appState.setPreRequestsParams(preRequestsParams);
  }

  private List<ConfigParams> mapToConfigParams(List<Request> preRequests) {
    return preRequests.stream()
        .map(Request::getConfigParams)
        .toList();
  }

  private ArrayList<ConfigParams> getUserPreRequestsParams(long currentUserId) {
    try {
      return new ArrayList<>(mapToConfigParams(requestService.getByUserIdAndType(currentUserId, PRE_REQUEST)));
    } catch (Exception ex) {
      log.error("Error fetching pre-requests for user [{}]", currentUserId, ex);
      ErrorNotification.show("Error fetching requests");
      throw ex;
    }
  }

  private void selectFirstItem() {
    var preRequestsParams = (List<ConfigParams>) dataProvider.getItems();
    if (preRequestsParams.isEmpty()) {
      return;
    }

    var firstRequestParam = preRequestsParams.getFirst();
    selectItem(firstRequestParam);
  }

  private void selectItem(ConfigParams preRequestParams) {
    selectedPreRequestParams = preRequestParams;
    applyPreRequest(preRequestParams);
    getSelectionModel().select(preRequestParams); // highlight item in the grid
  }

  private void addPreRequest(Long preRequestId) {
    var optPreRequest = requestService.getById(preRequestId);
    if (optPreRequest.isEmpty()) {
      return;
    }

    var newPreRequestParams = optPreRequest.get().getConfigParams();

    appState.addPreRequestParams(newPreRequestParams);

    selectItem(newPreRequestParams); // set new request as currently selected item
    focusPreRequestName();
  }

  private void showDeleteDialog(ConfigParams preRequestParams) {
    var deleteDialog = new ConfirmDialog();
    deleteDialog.setHeader("Delete request?");
    deleteDialog.setCancelable(true);
    deleteDialog.setConfirmText("Delete");
    deleteDialog.setCancelText("Cancel");
    deleteDialog.setConfirmButtonTheme("error primary");
    deleteDialog.addConfirmListener(event -> deletePreRequest(preRequestParams));

    deleteDialog.open();
  }

  private void deletePreRequest(ConfigParams toDelete) {
    try {
      requestService.delete(toDelete.getRequestId());
    } catch (Exception ex) {
      log.error("Error deleting pre-request {}", toDelete.getRequestId(), ex);
      ErrorNotification.show("Error deleting request");
      return;
    }

    var currentlySelected = getSelectionModel().getFirstSelectedItem();

    dataProvider.getItems().remove(toDelete);
    appState.removePreRequestParams(toDelete);

    // if the deleted item was currently selected, select another one (first in data provider)
    if (currentlySelected.isPresent() && currentlySelected.get().equals(toDelete)) {
      selectFirstItem();
    }
  }

}
