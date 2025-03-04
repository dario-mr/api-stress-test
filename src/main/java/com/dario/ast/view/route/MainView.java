package com.dario.ast.view.route;

import com.dario.ast.core.domain.StressTestConfig;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.core.service.AstUserService;
import com.dario.ast.core.service.SaveActionService;
import com.dario.ast.core.service.StressTestService;
import com.dario.ast.core.service.UserSessionService;
import com.dario.ast.view.component.config.ConfigLayout;
import com.dario.ast.view.component.headline.HeadlineLayout;
import com.dario.ast.view.component.run.RunLayout;
import com.dario.ast.view.component.sidebar.Sidebar;
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

  // TODO check if anything can be turned into a record
  // TODO folders...
  // TODO fix /h2-console access

  private final StressTestService stressTestService;
  private final AstRequestService astRequestService;
  private final UserSessionService userSessionService;
  private final AstUserService astUserService;
  private final SaveActionService saveActionService;

  @PostConstruct
  public void init() {
    setSizeFull();

    var stressTestConfig = new StressTestConfig(); // shared state object
    var configLayout = new ConfigLayout(saveActionService, stressTestConfig);
    var runLayout = new RunLayout(saveActionService, stressTestService, stressTestConfig);

    var requestLayout = new VerticalLayout(configLayout, runLayout);
    requestLayout.setPadding(false);

    var headline = new HeadlineLayout(userSessionService);

    var sidebar = new Sidebar(astRequestService, userSessionService, astUserService);

    var mainContent = new HorizontalLayout(sidebar, requestLayout);
    mainContent.setSizeFull();

    // add all components
    add(headline, mainContent);
  }
}