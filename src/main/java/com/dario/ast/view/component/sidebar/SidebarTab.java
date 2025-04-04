package com.dario.ast.view.component.sidebar;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class SidebarTab extends VerticalLayout {

  private static final String TAB_SIZE = "65px";

  public SidebarTab(VaadinIcon vaadinIcon, String tooltip) {
    setAlignItems(CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);
    setSpacing(false);
    setWidth(TAB_SIZE);
    setHeight(TAB_SIZE);
    getStyle()
        .set("cursor", "pointer")
        .set("border-radius", "var(--card-border-radius)");

    var icon = vaadinIcon.create();
    icon.setTooltipText(tooltip);

    add(icon);
  }

}
