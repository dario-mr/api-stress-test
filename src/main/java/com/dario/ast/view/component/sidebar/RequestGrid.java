package com.dario.ast.view.component.sidebar;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.event.AsrRequestCreatedEvent;
import com.dario.ast.event.AsrRequestUpdatedEvent;
import com.dario.ast.view.component.notification.ErrorNotification;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.dario.ast.util.EventUtil.*;

@Slf4j
public class RequestGrid extends Grid<AstRequest> {

    private final AstRequestService astRequestService;

    private ListDataProvider<AstRequest> dataProvider;

    public RequestGrid(AstRequestService astRequestService) {
        this.astRequestService = astRequestService;
        addClassName("requests-grid");

        // name column
        addColumn(astRequest -> astRequest.getConfigParams().getRequestName()).setAutoWidth(true);

        // click listener
        addItemClickListener(event -> {
            var astRequest = event.getItem();
            applyConfigParams(astRequest.getConfigParams());
            applyRunParams(astRequest.getRunParams());
        });

        loadRequests();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Ensure the event is fired only after the UI is fully initialized
        getUI().ifPresent(ui -> ui.access(this::selectFirstItem));

        // Listen for events indicating that the selected AST request was updated
        ComponentUtil.addListener(attachEvent.getUI(),
                AsrRequestUpdatedEvent.class,
                event -> updateAsrRequest(event.getAstRequest())
        );

        // Listen for events indicating that a new AST request was created
        ComponentUtil.addListener(attachEvent.getUI(),
                AsrRequestCreatedEvent.class,
                event -> addAsrRequest(event.getAstRequestId())
        );
    }

    private void loadRequests() {
        var astRequests = new ArrayList<AstRequest>();
        var currentUser = "user1@example.com"; // TODO use logged-in user

        try {
            astRequests = new ArrayList<>(astRequestService.getAstRequestsByEmail(currentUser)); // mutable list
        } catch (Exception ex) {
            log.error("Error fetching [{}] requests", currentUser, ex);
            ErrorNotification.show("Error fetching requests");
            throw new RuntimeException(ex);
        }

        dataProvider = new ListDataProvider<>(astRequests);
        setDataProvider(dataProvider);
    }

    private void selectFirstItem() {
        var astRequests = (List<AstRequest>) dataProvider.getItems();
        if (astRequests.isEmpty()) {
            return;
        }

        var firstRequest = astRequests.getFirst();
        selectItem(firstRequest);
    }

    private void selectItem(AstRequest firstRequest) {
        applyConfigParams(firstRequest.getConfigParams());
        applyRunParams(firstRequest.getRunParams());
        getSelectionModel().select(firstRequest); // highlight item in the grid
    }

    private void updateAsrRequest(AstRequest updatedRequest) {
        var optSelectedRequest = getSelectionModel().getFirstSelectedItem();
        if (optSelectedRequest.isEmpty()) {
            return;
        }

        var selectedRequest = optSelectedRequest.get();
        selectedRequest.setConfigParams(updatedRequest.getConfigParams());
        selectedRequest.setRunParams(updatedRequest.getRunParams());

        dataProvider.refreshItem(selectedRequest);
    }

    private void addAsrRequest(Long astRequestId) {
        var optAsrRequest = astRequestService.getAstRequestsById(astRequestId);
        if (optAsrRequest.isEmpty()) {
            return;
        }

        var newAsrRequest = optAsrRequest.get();

        dataProvider.getItems().add(newAsrRequest);
        dataProvider.refreshAll();
        selectItem(newAsrRequest); // set new request as currently selected item
        focusRequestName(); // focus the request name in Config layout
    }
}
