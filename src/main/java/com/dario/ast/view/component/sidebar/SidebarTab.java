package com.dario.ast.view.component.sidebar;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class SidebarTab extends VerticalLayout {

  public SidebarTab(VaadinIcon vaadinIcon, String name) {
    setAlignItems(CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);
    setSpacing(false);
    getStyle()
        .set("cursor", "pointer")
        .set("border-radius", "5px")
        .set("padding", "var(--lumo-space-s)");

    var icon = vaadinIcon.create();
    icon.getStyle().set("width", "16px").set("height", "16px");

    var title = new Span(name);
    title.getStyle().set("font-size", "0.8em");

    add(icon, title);
  }

}
