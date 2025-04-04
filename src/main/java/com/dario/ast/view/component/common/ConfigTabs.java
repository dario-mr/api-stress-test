package com.dario.ast.view.component.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import java.util.Map;

public class ConfigTabs extends VerticalLayout {

  /**
   * @param tabsMap Tabs map with structure: {label: content}
   */
  public ConfigTabs(Map<String, Component> tabsMap) {
    setSpacing(false);
    setPadding(false);

    // tabs
    var tabs = new Tabs();
    tabs.setWidthFull();
    tabsMap.forEach((label, component) -> tabs.add(new Tab(label)));

    // content area for tabs
    var content = new FlexLayout();
    content.setSizeFull();
    content.getStyle().set("padding-top", "var(--lumo-space-s)");

    // tab selection
    tabs.addSelectedChangeListener(event -> {
      content.removeAll();
      var selectedTab = event.getSelectedTab().getLabel();
      content.add(tabsMap.get(selectedTab));
      content.add(tabsMap.get(selectedTab));
    });

    // initial tab content
    content.add(tabsMap.values().iterator().next());

    add(tabs, content);
  }

}
