package com.dario.ast.view;

import com.dario.ast.core.domain.IndexedHttpStatus;
import com.dario.ast.core.service.StressService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@Route
@RequiredArgsConstructor
public class MainView extends VerticalLayout {

    private final StressService stressService;

    private final NumberField requestField = new NumberField("Number of requests");
    private final ListDataProvider<IndexedHttpStatus> resultDataProvider = new ListDataProvider<>(new ArrayList<>());
    private final ResultGrid resultGrid = new ResultGrid(resultDataProvider);

    @PostConstruct
    public void init() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        requestField.setMin(1);
        requestField.setValue(10d);

        var sendButton = new Button("Send Requests");
        sendButton.addClickListener(event -> sendRequests());

        var stopButton = new Button("Stop Requests");
        stopButton.addClickListener(event -> stressService.cancelAllRequests());

        add(requestField, sendButton, stopButton, resultGrid);
    }

    private void sendRequests() {
        int numRequests = requestField.getValue().intValue();
        resultDataProvider.getItems().clear();
        resultDataProvider.refreshAll();

        stressService.sendRequests(numRequests, result -> getUI().ifPresent(ui ->
                ui.access(() -> {
                    var currentItems = resultDataProvider.getItems();
                    var indexedResult = new IndexedHttpStatus(currentItems.size() + 1, result);
                    currentItems.add(indexedResult);

                    resultDataProvider.refreshAll();
                    resultGrid.scrollToEnd();
                })
        ));
    }
}
