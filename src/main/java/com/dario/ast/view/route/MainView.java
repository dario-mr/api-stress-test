package com.dario.ast.view.route;

import static com.dario.ast.core.domain.Section.ENVIRONMENTS;
import static com.dario.ast.core.domain.Section.REQUESTS;
import static com.dario.ast.core.domain.Section.PRE_REQUESTS;

import com.dario.ast.view.component.config.ConfigLayout;
import com.dario.ast.view.component.environment.EnvironmentLayout;
import com.dario.ast.view.component.headline.HeadlineLayout;
import com.dario.ast.view.component.run.RunLayout;
import com.dario.ast.view.component.prerequest.PreRequestLayout;
import com.dario.ast.view.component.sidebar.MainSidebar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;

@Route
@PermitAll
@RequiredArgsConstructor
@PageTitle("API Stress Test")
public class MainView extends VerticalLayout {

  // TODO folders
  // TODO refresh user session (remember-me)
  // TODO move all color to themes

  private final HeadlineLayout headlineLayout;
  private final MainSidebar mainSidebar;
  private final ConfigLayout configLayout;
  private final RunLayout runLayout;
  private final EnvironmentLayout environmentLayout;
  private final PreRequestLayout preRequestLayout;

  @PostConstruct
  public void init() {
    setSizeFull();
    getStyle()
        .set("gap", "var(--lumo-space-s)")
        .set("padding", "var(--lumo-space-s)");

    var requestLayout = new VerticalLayout(configLayout, runLayout);
    requestLayout.getStyle().set("gap", "var(--lumo-space-s)");
    requestLayout.setPadding(false);

    var mainContent = new HorizontalLayout(mainSidebar, requestLayout, environmentLayout, preRequestLayout);
    mainContent.getStyle().set("gap", "var(--lumo-space-s)");
    mainContent.setSizeFull();

    // initially show only requestLayout
    requestLayout.setVisible(true);
    environmentLayout.setVisible(false);
    preRequestLayout.setVisible(false);

    // set selection listener to show correct layout on tab click
    mainSidebar.setSelectionListener(selectedSection -> {
      requestLayout.setVisible(selectedSection == REQUESTS);
      environmentLayout.setVisible(selectedSection == ENVIRONMENTS);
      preRequestLayout.setVisible(selectedSection == PRE_REQUESTS);
    });

    // add all components
    add(headlineLayout, mainContent);
  }

}