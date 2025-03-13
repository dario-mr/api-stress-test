package com.dario.ast.view.route;

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
  // TODO create other sidebar with Requests, Environments, which will load data in current sidebar (probably 2 sidebar components)
  // TODO env variables

  private final HeadlineLayout headlineLayout;
  private final ConfigLayout configLayout;
  private final RunLayout runLayout;
  private final Sidebar sidebar;

  @PostConstruct
  public void init() {
    setSizeFull();

    var requestLayout = new VerticalLayout(configLayout, runLayout);
    requestLayout.setPadding(false);

    var mainContent = new HorizontalLayout(sidebar, requestLayout);
    mainContent.setSizeFull();

    // add all components
    add(headlineLayout, mainContent);
  }
}