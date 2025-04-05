package com.dario.ast.view.component.sidebar;

import static com.dario.ast.core.domain.Section.ENVIRONMENTS;
import static com.dario.ast.core.domain.Section.PRE_REQUESTS;
import static com.dario.ast.core.domain.Section.REQUESTS;
import static com.vaadin.flow.component.icon.VaadinIcon.CUBES;
import static com.vaadin.flow.component.icon.VaadinIcon.MAGIC;
import static com.vaadin.flow.component.icon.VaadinIcon.PAPERPLANE;

import com.dario.ast.core.domain.Section;
import com.dario.ast.view.component.sidebar.environment.EnvSidebar;
import com.dario.ast.view.component.sidebar.prerequest.PreRequestSidebar;
import com.dario.ast.view.component.sidebar.request.RequestSidebar;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.function.Consumer;
import lombok.Setter;

@UIScope
@SpringComponent
public class MainSidebar extends HorizontalLayout {

  private final RequestSidebar requestSidebar;
  private final EnvSidebar envSidebar;
  private final PreRequestSidebar preRequestSidebar;

  private final SidebarTab requestsTab;
  private final SidebarTab envsTab;
  private final SidebarTab preRequestsTab;

  @Setter
  private Consumer<Section> selectionListener;

  public MainSidebar(RequestSidebar requestSidebar, EnvSidebar envSidebar, PreRequestSidebar preRequestSidebar) {
    this.requestSidebar = requestSidebar;
    this.envSidebar = envSidebar;
    this.preRequestSidebar = preRequestSidebar;

    setPadding(false);
    setSpacing(false);
    getStyle().set("gap", "var(--lumo-space-s)");

    requestsTab = new SidebarTab(PAPERPLANE, "Requests");
    requestsTab.addClickListener(e -> selectTab(requestSidebar, requestsTab, REQUESTS));

    envsTab = new SidebarTab(CUBES, "Environments");
    envsTab.addClickListener(e -> selectTab(envSidebar, envsTab, ENVIRONMENTS));

    preRequestsTab = new SidebarTab(MAGIC, "Pre-Requests");
    preRequestsTab.addClickListener(e -> selectTab(preRequestSidebar, preRequestsTab, PRE_REQUESTS));

    var sectionsLayout = new VerticalLayout(requestsTab, envsTab, preRequestsTab);
    sectionsLayout.addClassNames("card-layout", "main-sidebar");
    sectionsLayout.setSpacing(false);

    add(sectionsLayout, requestSidebar, envSidebar, preRequestSidebar);

    // pre-select Requests sidebar
    selectTab(requestSidebar, requestsTab, REQUESTS);
  }

  private void selectTab(Component sidebarToShow, VerticalLayout selectedTab, Section selectedSection) {
    requestSidebar.setVisible(sidebarToShow == requestSidebar);
    envSidebar.setVisible(sidebarToShow == envSidebar);
    preRequestSidebar.setVisible(sidebarToShow == preRequestSidebar);

    // reset tab background colors and highlight selected tab
    requestsTab.getStyle().set("background-color", "transparent");
    envsTab.getStyle().set("background-color", "transparent");
    preRequestsTab.getStyle().set("background-color", "transparent");
    selectedTab.getStyle().set("background-color", "rgba(255, 255, 255, 0.1)");

    // communicate to MainView what tab is selected
    if (selectionListener != null) {
      selectionListener.accept(selectedSection);
    }
  }

}
