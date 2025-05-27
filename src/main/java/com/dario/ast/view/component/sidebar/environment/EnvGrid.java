package com.dario.ast.view.component.sidebar.environment;

import static com.dario.ast.util.EventUtil.applyEnvironment;
import static com.dario.ast.util.EventUtil.focusEnvName;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.service.EnvironmentService;
import com.dario.ast.event.created.EnvironmentCreatedEvent;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.dario.ast.view.component.common.reactive.ReactiveComponent;
import com.dario.ast.view.component.common.reactive.ReactiveHandler;
import com.dario.ast.view.component.common.reactive.ReactiveType;
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
@ReactiveComponent
public class EnvGrid extends Grid<Environment> {

  private final EnvironmentService environmentService;
  private final AppState appState;

  private ListDataProvider<Environment> dataProvider;
  private Environment selectedEnvironment;
  private boolean firstLoad = true;

  public EnvGrid(EnvironmentService environmentService, AppState appState) {
    this.environmentService = environmentService;
    this.appState = appState;

    addClassName("sidebar-grid");

    // name column
    addColumn(Environment::getName).setAutoWidth(true).setFlexGrow(1);

    // delete button
    addDeleteColumn();

    // click listener to load selected env into UI
    addItemClickListener(event -> {
      var env = event.getItem();
      selectItem(env);
    });

    loadEnvs();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Listen for events indicating that a new Environment was created
    ComponentUtil.addListener(attachEvent.getUI(),
        EnvironmentCreatedEvent.class,
        event -> addEnv(event.getEnvironmentId())
    );
  }

  @ReactiveHandler(ReactiveType.ENVIRONMENTS_LIST)
  public void onEnvironmentsChanged(List<Environment> environments) {
    dataProvider = new ListDataProvider<>(environments);
    setDataProvider(dataProvider);

    if (selectedEnvironment != null) {
      getSelectionModel().select(selectedEnvironment);
    }
    if (firstLoad) {
      firstLoad = false;
      selectFirstItem();
    }
  }

  private void addDeleteColumn() {
    addColumn(new ComponentRenderer<>(env -> {
      var deleteButton = new Button(TRASH.create(), e -> showDeleteDialog(env));
      deleteButton.addClassName("delete-button");
      return deleteButton;
    }))
        .setAutoWidth(true)
        .setFlexGrow(0);
  }

  private void loadEnvs() {
    var currentUserId = appState.getCurrentUser().getId();
    var userEnvironments = getUserEnvironments(currentUserId);

    appState.setEnvironments(userEnvironments);
  }

  private ArrayList<Environment> getUserEnvironments(long currentUserId) {
    try {
      return new ArrayList<>(environmentService.getByUserId(currentUserId)); // mutable list
    } catch (Exception ex) {
      log.error("Error fetching environments for user [{}]", currentUserId, ex);
      ErrorNotification.show("Error fetching environments");
      throw ex;
    }
  }

  private void selectFirstItem() {
    var environments = (List<Environment>) dataProvider.getItems();
    if (environments.isEmpty()) {
      return;
    }

    var firstEnv = environments.getFirst();
    selectItem(firstEnv);
  }

  private void selectItem(Environment environment) {
    selectedEnvironment = environment;
    applyEnvironment(environment);
    getSelectionModel().select(environment); // highlight item in the grid
  }

  private void addEnv(Long envId) {
    var optEnv = environmentService.getById(envId);
    if (optEnv.isEmpty()) {
      return;
    }

    var newEnv = optEnv.get();

    appState.addEnvironment(newEnv);

    selectItem(newEnv); // set new environment as currently selected item
    focusEnvName();
  }

  private void showDeleteDialog(Environment env) {
    var deleteDialog = new ConfirmDialog();
    deleteDialog.setHeader("Delete environment?");
    deleteDialog.setCancelable(true);
    deleteDialog.setConfirmText("Delete");
    deleteDialog.setCancelText("Cancel");
    deleteDialog.setConfirmButtonTheme("error primary");
    deleteDialog.addConfirmListener(event -> deleteEnvironment(env));

    deleteDialog.open();
  }

  private void deleteEnvironment(Environment toDelete) {
    try {
      environmentService.delete(toDelete.getId());
    } catch (Exception ex) {
      log.error("Error deleting environment {}", toDelete.getId(), ex);
      ErrorNotification.show("Error deleting environment");
      return;
    }

    var selectedEnv = getSelectionModel().getFirstSelectedItem();

    dataProvider.getItems().remove(toDelete);
    appState.removeEnvironment(toDelete);

    // if the deleted item was currently selected, select another one (first in data provider)
    if (selectedEnv.isPresent() && selectedEnv.get().equals(toDelete)) {
      selectFirstItem();
    }
  }

}
