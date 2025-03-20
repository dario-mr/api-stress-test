package com.dario.ast.view.component.config;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ToggleLayout extends VerticalLayout {

  private final VerticalLayout contentLayout;
  private final Button toggleButton;
  private final Icon expandIcon;
  private final Icon collapseIcon;

  public ToggleLayout(String title, Component... components) {
    setSpacing(false);
    setPadding(false);

    expandIcon = VaadinIcon.ANGLE_DOWN.create();
    collapseIcon = VaadinIcon.ANGLE_RIGHT.create();

    toggleButton = new Button(collapseIcon);
    toggleButton.getStyle().set("background", "none");
    toggleButton.getStyle().set("margin", "0");
    toggleButton.getStyle().set("padding-left", "0");
    toggleButton.addClickListener(event -> toggleContentVisibility());

    var titleText = new H5(title);
    var titleLayout = new HorizontalLayout(toggleButton, titleText);
    titleLayout.setVerticalComponentAlignment(CENTER, titleText);
    titleLayout.setSpacing(false);

    contentLayout = new VerticalLayout(components);
    contentLayout.setPadding(false);
    contentLayout.setVisible(false);

    add(titleLayout, contentLayout);
  }

  private void toggleContentVisibility() {
    boolean isVisible = contentLayout.isVisible();

    contentLayout.setVisible(!isVisible);
    toggleButton.setIcon(isVisible ? collapseIcon : expandIcon);
  }
}
