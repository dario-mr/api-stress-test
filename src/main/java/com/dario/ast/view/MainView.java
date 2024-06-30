package com.dario.ast.view;

import com.dario.ast.core.domain.StressRequest;
import com.dario.ast.core.service.StressService;
import com.dario.ast.proxy.ApiResponse;
import com.dario.ast.view.component.Headline;
import com.dario.ast.view.component.ToggleLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.values;
import static org.springframework.util.StringUtils.hasText;

@Route
@RequiredArgsConstructor
@PageTitle("API Stress Test")
public class MainView extends VerticalLayout {
    // TODO can this class be cleaned?
    // TODO save parameters for re-use
    // TODO request preview (curl format?)

    private final StressService stressService;

    private final TextField urlText = new TextField("API endpoint");
    private final ComboBox<HttpMethod> methodCombo = new ComboBox<>("Method", values());
    private final VerticalLayout headersLayout = new VerticalLayout();
    private final VerticalLayout uriVariablesLayout = new VerticalLayout();
    private final VerticalLayout queryParamsLayout = new VerticalLayout();
    private final VerticalLayout requestBody = new VerticalLayout();
    private final TextArea requestBodyText = new TextArea();
    private final IntegerField requestNumberField = new IntegerField("Requests");
    private final IntegerField threadPoolSizeField = new IntegerField("Threads");
    private final TextField completedText = new TextField("Completed");
    private final TextField failedText = new TextField("Failed");
    private final TextField errorText = new TextField("Errors");
    private final Button startButton = new Button("START");
    private final Button stopButton = new Button("STOP");
    private final Checkbox stopOnError = new Checkbox("Stop on error");

    private long completedRequests = 0, failedRequests = 0;

    @PostConstruct
    public void init() {
        urlText.setValue("https://www.bing.com");
        urlText.setWidthFull();

        methodCombo.setValue(GET);
        methodCombo.setWidth("8em");

        var urlLayout = new HorizontalLayout(urlText, methodCombo);
        urlLayout.setWidthFull();
        urlLayout.getStyle().set("margin-bottom", "1em");

        headersLayout.setPadding(false);
        addKeyValueRow(headersLayout);

        uriVariablesLayout.setPadding(false);
        addKeyValueRow(uriVariablesLayout);

        queryParamsLayout.setPadding(false);
        addKeyValueRow(queryParamsLayout);

        requestBodyText.setPlaceholder("Request Body");
        requestBodyText.setWidth("60%");
        requestBody.setPadding(false);
        requestBody.add(requestBodyText);

        requestNumberField.setMin(1);
        requestNumberField.setValue(10);
        requestNumberField.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                requestNumberField.setValue(event.getOldValue());
            }
            if (event.getValue() < 1) {
                requestNumberField.setValue(1);
            }
        });

        threadPoolSizeField.setMin(1);
        threadPoolSizeField.setValue(12);
        threadPoolSizeField.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                threadPoolSizeField.setValue(event.getOldValue());
            }
            if (event.getValue() < 1) {
                threadPoolSizeField.setValue(1);
            }
        });

        startButton.addClickListener(event -> startStressTest());
        stopButton.addClickListener(event -> stopStressTest());
        stopButton.setVisible(false);
        stopOnError.setValue(true);

        completedText.setReadOnly(true);
        failedText.setReadOnly(true);
        failedText.setVisible(false);
        errorText.setReadOnly(true);
        errorText.setWidthFull();
        errorText.setVisible(false);

        var runContent = new HorizontalLayout(requestNumberField, threadPoolSizeField, stopOnError, startButton, stopButton);
        runContent.setVerticalComponentAlignment(END, stopOnError, startButton, stopButton);

        var runLayout = new VerticalLayout(new H3("Run"), runContent);
        runLayout.setPadding(false);
        runLayout.setSpacing(false);
        runLayout.getStyle().set("margin-bottom", "1em");

        var resultsContent = new HorizontalLayout(completedText, failedText, errorText);
        resultsContent.setWidthFull();
        var resultsLayout = new VerticalLayout(new H3("Results"), resultsContent);
        resultsLayout.setPadding(false);
        resultsLayout.setSpacing(false);

        var container = new VerticalLayout(
                new Headline(),
                urlLayout,
                new ToggleLayout("Headers", headersLayout),
                new ToggleLayout("URI Variables", uriVariablesLayout),
                new ToggleLayout("Query Parameters", queryParamsLayout),
                new ToggleLayout("Request Body", requestBody),
                runLayout,
                resultsLayout);
        container.setMaxWidth("1000px");

        add(container);
        setAlignItems(CENTER);
        setPadding(false);
    }

    private void startStressTest() {
        stopStressTest();
        startStressTestUI();

        var numRequests = requestNumberField.getValue();
        var threadPoolSize = threadPoolSizeField.getValue();
        var url = urlText.getValue();
        var method = methodCombo.getValue();
        var headers = collectMap(headersLayout);
        var uriVariables = collectMap(uriVariablesLayout);
        var queryParams = collectMap(queryParamsLayout);
        var requestBody = requestBodyText.getValue();

        stressService.startStressTest(
                new StressRequest(
                        numRequests,
                        url, method,
                        headers, uriVariables, queryParams,
                        requestBody,
                        result -> getUI().ifPresent(ui -> ui.access(() -> applyResponse(result)))
                ), Executors.newFixedThreadPool(threadPoolSize));
    }

    private void stopStressTest() {
        stressService.cancelStressTest();

        completedRequests = 0;
        failedRequests = 0;

        requestNumberField.setEnabled(true);
        threadPoolSizeField.setEnabled(true);
        stopOnError.setEnabled(true);
        startButton.setVisible(true);
        stopButton.setVisible(false);
    }

    private void startStressTestUI() {
        requestNumberField.setEnabled(false);
        threadPoolSizeField.setEnabled(false);
        stopOnError.setEnabled(false);
        startButton.setVisible(false);
        stopButton.setVisible(true);

        completedText.setValue("");
        failedText.setValue("");
        failedText.setVisible(false);
        errorText.setValue("");
        errorText.setVisible(false);
    }

    private void applyResponse(ApiResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            completedRequests++;
            completedText.setValue(String.valueOf(completedRequests));
        } else {
            failedRequests++;
            failedText.setValue(String.valueOf(failedRequests));
            errorText.setValue(response.statusCode().value() + " - " + response.errorMessage());
            failedText.setVisible(true);
            errorText.setVisible(true);

            if (stopOnError.getValue()) {
                stopStressTest();
            }
        }

        // when stress test is completed, update UI to reflect it
        if (completedRequests + failedRequests == requestNumberField.getValue()) {
            stopStressTest();
        }
    }

    private void addKeyValueRow(VerticalLayout target) {
        var keyField = new TextField();
        var valueField = new TextField();
        var removeButton = new Button();
        var row = new HorizontalLayout(keyField, valueField, removeButton);

        keyField.setPlaceholder("Key");
        keyField.addValueChangeListener(changeEvent -> {
            if (hasText(changeEvent.getValue()) && !thereIsAnEmptyHeader(target)) {
                addKeyValueRow(target);
            }
        });

        valueField.setPlaceholder("Value");

        removeButton.setIcon(TRASH.create());
        removeButton.addClickListener(event -> {
            if (target.getChildren().count() > 1) {
                target.remove(row);
            }
        });

        row.setVerticalComponentAlignment(END, removeButton);
        target.add(row);
    }

    private boolean thereIsAnEmptyHeader(VerticalLayout parent) {
        return parent.getChildren().anyMatch(component -> {
            var layout = (HorizontalLayout) component;
            var keyField = (TextField) layout.getComponentAt(0);
            var valueField = (TextField) layout.getComponentAt(1);

            var key = keyField.getValue();
            var value = valueField.getValue();

            return key.isEmpty() && value.isEmpty();
        });
    }

    private Map<String, String> collectMap(VerticalLayout parent) {
        var map = new HashMap<String, String>();

        parent.getChildren().forEach(component -> {
            var layout = (HorizontalLayout) component;
            var keyField = (TextField) layout.getComponentAt(0);
            var valueField = (TextField) layout.getComponentAt(1);

            var key = keyField.getValue();
            var value = valueField.getValue();
            if (!key.isEmpty() && !value.isEmpty()) {
                map.put(key, value);
            }
        });

        return map;
    }
}