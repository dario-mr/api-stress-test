package com.dario.ast.view.component.headline;

import static com.dario.ast.core.domain.LocalSettings.THEME;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;

import com.dario.ast.view.component.environment.EnvironmentCombo;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.WebStorage;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.Lumo;

@UIScope
@SpringComponent
@CssImport(value = "./styles/headline-layout.css")
public class HeadlineLayout extends HorizontalLayout {

  public HeadlineLayout(UserAvatar userAvatar, EnvironmentCombo environmentCombo) {
    setWidthFull();
    setAlignItems(CENTER);

    // headline
    var headline = new Headline();

    // spacer
    var spacer = new Div();
    spacer.getStyle().set("flex-grow", "1");

    // theme icons
    var lightThemeIcon = VaadinIcon.SUN_O.create();
    var darkThemeIcon = VaadinIcon.MOON_O.create();
    configureThemeIcons(lightThemeIcon, darkThemeIcon);

    // add all components
    add(headline, spacer, environmentCombo, lightThemeIcon, darkThemeIcon, userAvatar);
  }

  private void configureThemeIcons(Icon lightThemeIcon, Icon darkThemeIcon) {
    // by default, hide both icons
    lightThemeIcon.setVisible(false);
    lightThemeIcon.getStyle()
        .set("cursor", "pointer")
        .set("flex-shrink", "0");
    darkThemeIcon.setVisible(false);
    darkThemeIcon.getStyle()
        .set("cursor", "pointer")
        .set("flex-shrink", "0");

    // add listeners to switch theme
    lightThemeIcon.addClickListener(e -> {
      lightThemeIcon.setVisible(false);
      darkThemeIcon.setVisible(true);
      setTheme("dark");
    });
    darkThemeIcon.addClickListener(e -> {
      lightThemeIcon.setVisible(true);
      darkThemeIcon.setVisible(false);
      setTheme("light");
    });

    // load theme setting and show the right theme icon
    WebStorage.getItem(THEME.getName(), value -> {
      var theme = value != null ? value : THEME.getDefaultValue();
      if (theme.equals("dark")) {
        lightThemeIcon.setVisible(false);
        darkThemeIcon.setVisible(true);
      } else {
        lightThemeIcon.setVisible(true);
        darkThemeIcon.setVisible(false);
      }
    });
  }

  private void setTheme(String theme) {
    // save theme setting in local storage
    WebStorage.setItem(THEME.getName(), String.valueOf(theme));

    // set theme in UI
    var js = "document.documentElement.setAttribute('theme', $0)";
    getElement().executeJs(js, theme.equals("dark") ? Lumo.DARK : Lumo.LIGHT);
  }

}
