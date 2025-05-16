package com.dario.ast.view.route;

import static com.dario.ast.core.domain.LocalSettings.THEME;
import static com.dario.ast.core.domain.Section.ENVIRONMENTS;
import static com.dario.ast.core.domain.Section.PRE_REQUESTS;
import static com.dario.ast.core.domain.Section.REQUESTS;

import com.dario.ast.view.component.environment.EnvironmentLayout;
import com.dario.ast.view.component.folder.FolderLayout;
import com.dario.ast.view.component.headline.HeadlineLayout;
import com.dario.ast.view.component.prerequest.PreRequestLayout;
import com.dario.ast.view.component.request.RequestLayout;
import com.dario.ast.view.component.sidebar.MainSidebar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.WebStorage;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;

@Route
@PermitAll
@RequiredArgsConstructor
@PageTitle("API Stress Test")
public class MainView extends VerticalLayout {

  // TODO button to duplicate requests
  // TODO bug: when creating a new key in EntriesSection, sometimes focus is lost

  private final HeadlineLayout headlineLayout;
  private final MainSidebar mainSidebar;
  private final RequestLayout requestLayout;
  private final FolderLayout folderLayout;
  private final EnvironmentLayout environmentLayout;
  private final PreRequestLayout preRequestLayout;

  @PostConstruct
  public void init() {
    setTheme();
    setSizeFull();
    getStyle()
        .set("gap", "var(--lumo-space-s)")
        .set("padding", "var(--lumo-space-s)");

    var mainContent = new HorizontalLayout(mainSidebar, requestLayout, folderLayout, environmentLayout,
        preRequestLayout);
    mainContent.getStyle().set("gap", "var(--lumo-space-s)");
    mainContent.setSizeFull();

    // initially show only requestLayout
    requestLayout.setVisible(true);
    folderLayout.setVisible(false);
    environmentLayout.setVisible(false);
    preRequestLayout.setVisible(false);

    // set selection listener to show correct layout on tab click
    mainSidebar.setSelectionListener(selectedSection -> {
      folderLayout.setVisible(false);
      requestLayout.setVisible(selectedSection == REQUESTS);
      environmentLayout.setVisible(selectedSection == ENVIRONMENTS);
      preRequestLayout.setVisible(selectedSection == PRE_REQUESTS);
    });

    // add all components
    add(headlineLayout, mainContent);
  }

  private void setTheme() {
    WebStorage.getItem(THEME.getName(), value -> {
      var theme = value != null ? value : THEME.getDefaultValue();
      var js = "document.documentElement.setAttribute('theme', $0)";

      getElement().executeJs(js, theme.equals("dark") ? Lumo.DARK : Lumo.LIGHT);
    });
  }

}