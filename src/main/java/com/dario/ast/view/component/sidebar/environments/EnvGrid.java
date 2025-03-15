package com.dario.ast.view.component.sidebar.environments;

import static com.dario.ast.util.EventUtil.applyEnvironment;
import static com.dario.ast.util.EventUtil.environmentDeleted;
import static com.dario.ast.util.EventUtil.focusEnvName;
import static com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

import com.dario.ast.core.domain.ApplicationState;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.service.AstEnvironmentService;
import com.dario.ast.event.EnvironmentCreatedEvent;
import com.dario.ast.event.EnvironmentUpdatedEvent;
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
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@UIScope
@SpringComponent
public class EnvGrid extends Grid<Environment> {

  private final AstEnvironmentService astEnvironmentService;
  private final ApplicationState applicationState;

  private ListDataProvider<Environment> dataProvider;
  private Environment lastSelectedItem;

  @Autowired
  public EnvGrid(AstEnvironmentService astEnvironmentService, ApplicationState applicationState) {
    this.astEnvironmentService = astEnvironmentService;
    this.applicationState = applicationState;

    addClassName("sidebar-grid");

    // name column
    addColumn(Environment::getName).setAutoWidth(true).setFlexGrow(1);

    // delete button
    addDeleteColumn();

    // click listener to load selected env into UI
    addItemClickListener(event -> {
      var env = event.getItem();
      applyEnvironment(env);
    });

    preventUnselection();
    loadEnvs();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Ensure the event is fired only after the UI is fully initialized
    getUI().ifPresent(ui -> ui.access(this::selectFirstItem));

    // Listen for events indicating that the selected Environment was updated
    ComponentUtil.addListener(attachEvent.getUI(),
        EnvironmentUpdatedEvent.class,
        event -> updateEnvironment(event.getEnvironment())
    );

    // Listen for events indicating that a new Environment was created
    ComponentUtil.addListener(attachEvent.getUI(),
        EnvironmentCreatedEvent.class,
        event -> addEnv(event.getEnvironmentId())
    );
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

  private void loadEnvs() {
    var currentUserId = applicationState.getCurrentUser().getId();
    var userEnvironments = getUserEnvironments(currentUserId);

    dataProvider = new ListDataProvider<>(userEnvironments);
    setDataProvider(dataProvider);
  }

  private ArrayList<Environment> getUserEnvironments(long currentUserId) {
    try {
      return new ArrayList<>(astEnvironmentService.getByUserId(currentUserId)); // mutable list
    } catch (Exception ex) {
      log.error("Error fetching environments for user [{}]", currentUserId, ex);
      ErrorNotification.show("Error fetching environments");
      throw new RuntimeException(ex);
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
    applyEnvironment(environment);
    getSelectionModel().select(environment); // highlight item in the grid
  }

  // if an environment is updated by some other component, we need to refresh it in the grid data provider
  private void updateEnvironment(Environment updatedEnv) {
    // find the updated environment in the data provider (by environment id)
    var currentEnvOpt = dataProvider.getItems().stream()
        .filter(env -> env.getId().equals(updatedEnv.getId()))
        .findFirst();
    if (currentEnvOpt.isEmpty()) {
      return;
    }

    var currentEnv = currentEnvOpt.get();
    currentEnv.setName(updatedEnv.getName());
    currentEnv.setVariables(updatedEnv.getVariables());

    dataProvider.refreshItem(currentEnv);
  }

  private void addEnv(Long envId) {
    var optEnv = astEnvironmentService.getById(envId);
    if (optEnv.isEmpty()) {
      return;
    }

    var newEnv = optEnv.get();

    dataProvider.getItems().add(newEnv);
    dataProvider.refreshAll();
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
      astEnvironmentService.delete(toDelete.getId());
    } catch (Exception ex) {
      log.error("Error deleting environment {}", toDelete.getId(), ex);
      ErrorNotification.show("Error deleting environment");
      return;
    }

    var selectedEnv = getSelectionModel().getFirstSelectedItem();

    // update data provider
    dataProvider.getItems().remove(toDelete);
    dataProvider.refreshAll();

    // if the deleted item was currently selected, select another one (first in data provider)
    if (selectedEnv.isPresent() && selectedEnv.get().equals(toDelete)) {
      selectFirstItem();
    }

    environmentDeleted(toDelete.getId()); // notify other components of the environment deletion
  }

}
