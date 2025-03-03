package com.dario.ast.view.route;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.core.service.AstUserService;
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
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@Route
@PermitAll
@RequiredArgsConstructor
@PageTitle("API Stress Test")
public class MainView extends VerticalLayout {

  // TODO check if anything can be turned into a record
  // TODO remove save button, save automatically on value change
  // TODO folders...
  // TODO fix /h2-console access

  private final StressTestService stressTestService;
  private final AstRequestService astRequestService;
  private final UserSessionService userSessionService;
  private final AstUserService astUserService;

  @PostConstruct
  public void init() {
    setSizeFull();

    var configLayout = new ConfigLayout();
    var configParamsSupplier = (Supplier<ConfigParams>) configLayout::getConfigParams;

    var runLayout = new RunLayout(stressTestService, configParamsSupplier);
    var runParamsSupplier = (Supplier<RunParams>) runLayout::getRunParams;

    var requestLayout = new VerticalLayout(configLayout, runLayout);
    requestLayout.setPadding(false);

    var headline = new HeadlineLayout(astRequestService, userSessionService, configParamsSupplier, runParamsSupplier);

    var sidebar = new Sidebar(astRequestService, userSessionService, astUserService);

    var mainContent = new HorizontalLayout(sidebar, requestLayout);
    mainContent.setSizeFull();

    // add all components
    add(headline, mainContent);
  }
}