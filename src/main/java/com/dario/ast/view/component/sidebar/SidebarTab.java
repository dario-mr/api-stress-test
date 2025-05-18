package com.dario.ast.view.component.sidebar;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class SidebarTab extends VerticalLayout {

  public SidebarTab(VaadinIcon vaadinIcon, String tabTitle) {
    addClassName("main-sidebar-tab");
    setAlignItems(CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);
    setSpacing(false);

    var icon = vaadinIcon.create();

    var title = new Span(tabTitle);
    title.addClassName("main-sidebar-title");

    add(icon, title);
  }

}
