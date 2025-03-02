package com.dario.ast.view.component.sidebar;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.service.AstStorageService;
import com.dario.ast.view.component.notification.ErrorNotification;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class RequestGrid extends Grid<AstRequest> {

    // TODO add click listener to load selected request into UI

    private final AstStorageService astStorageService;

    private ListDataProvider<AstRequest> dataProvider;

    public RequestGrid(AstStorageService astStorageService) {
        this.astStorageService = astStorageService;
        addClassName("requests-grid");

        // add columns
        addColumn(AstRequest::getName).setAutoWidth(true);

        loadRequests();
    }

    private void loadRequests() {
        var astRequests = new ArrayList<AstRequest>();
        var currentUser = "user1@example.com"; // TODO use logged-in user

        try {
            astRequests = new ArrayList<>(astStorageService.getAstRequestsByEmail(currentUser)); // mutable list
        } catch (Exception ex) {
            log.error("Error loading user's {} requests", currentUser, ex);
            ErrorNotification.show("Error loading your requests");
        }

        dataProvider = new ListDataProvider<>(astRequests);
        setDataProvider(dataProvider);
    }
}
