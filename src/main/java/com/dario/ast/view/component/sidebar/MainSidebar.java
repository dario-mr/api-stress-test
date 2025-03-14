package com.dario.ast.view.component.sidebar;

import static com.dario.ast.core.domain.SidebarSection.ENVIRONMENTS;
import static com.dario.ast.core.domain.SidebarSection.REQUESTS;
import static com.vaadin.flow.component.icon.VaadinIcon.ENVELOPES;
import static com.vaadin.flow.component.icon.VaadinIcon.GLOBE;

import com.dario.ast.core.domain.SidebarSection;
import com.dario.ast.view.component.sidebar.environments.EnvSidebar;
import com.dario.ast.view.component.sidebar.requests.RequestSidebar;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.function.Consumer;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@UIScope
@SpringComponent
public class MainSidebar extends HorizontalLayout {

  public static final String MIN_SIDEBAR_WIDTH = "250px";
  public static final String MAX_SIDEBAR_WIDTH = "350px";

  private final RequestSidebar requestSidebar;
  private final EnvSidebar envSidebar;
  private final SidebarTab requestsTab;
  private final SidebarTab envsTab;

  @Setter
  private Consumer<SidebarSection> selectionListener;

  @Autowired
  public MainSidebar(RequestSidebar requestSidebar, EnvSidebar envSidebar) {
    this.requestSidebar = requestSidebar;
    this.envSidebar = envSidebar;

    setPadding(false);
    setSpacing(false);
    getStyle().set("gap", "var(--lumo-space-s)");

    requestsTab = new SidebarTab(ENVELOPES, "Requests");
    requestsTab.addClickListener(e -> selectTab(requestSidebar, requestsTab, REQUESTS));

    envsTab = new SidebarTab(GLOBE, "Environments");
    envsTab.addClickListener(e -> selectTab(envSidebar, envsTab, ENVIRONMENTS));

    var sectionsLayout = new VerticalLayout(requestsTab, envsTab);
    sectionsLayout.setPadding(false);
    sectionsLayout.setSpacing(false);

    add(sectionsLayout, requestSidebar, envSidebar);

    // pre-select Requests sidebar
    selectTab(requestSidebar, requestsTab, REQUESTS);
  }

  private void selectTab(Component sidebarToShow, VerticalLayout selectedTab, SidebarSection section) {
    requestSidebar.setVisible(sidebarToShow == requestSidebar);
    envSidebar.setVisible(sidebarToShow == envSidebar);

    // reset tab background colors and highlight selected tab
    requestsTab.getStyle().set("background-color", "transparent");
    envsTab.getStyle().set("background-color", "transparent");
    selectedTab.getStyle().set("background-color", "rgba(255, 255, 255, 0.1)");

    // communicate to MainView what tab is selected
    if (selectionListener != null) {
      selectionListener.accept(section);
    }
  }

}
