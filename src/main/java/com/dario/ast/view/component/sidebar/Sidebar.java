package com.dario.ast.view.component.sidebar;

import com.dario.ast.core.service.AstRequestService;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@CssImport(value = "./styles/sidebar.css")
public class Sidebar extends VerticalLayout {

    private final static String MAX_SIDEBAR_WIDTH = "250px";

    public Sidebar(AstRequestService astRequestService) {
        setWidthFull();
        setPadding(false);
        setMaxWidth(MAX_SIDEBAR_WIDTH);
        addClassNames("card-layout", "sidebar-layout");

        // title + create button
        var title = new H3("My Requests");
        title.addClassName("sidebar-title");
        var createButton = new CreateButton(astRequestService);

        var titleCreateLayout = new HorizontalLayout(title, createButton);

        // requests grid
        var requestsGrid = new RequestGrid(astRequestService);

        add(titleCreateLayout, requestsGrid);
    }
}
