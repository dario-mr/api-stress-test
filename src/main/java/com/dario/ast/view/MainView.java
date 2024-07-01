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

import static com.dario.ast.util.IntegerFieldUtil.integerValidationListener;
import static com.dario.ast.util.EntryUtil.addEntry;
import static com.dario.ast.util.EntryUtil.collectMap;
import static com.vaadin.flow.component.icon.VaadinIcon.PLAY;
import static com.vaadin.flow.component.icon.VaadinIcon.STOP;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.values;

@Route
@RequiredArgsConstructor
@PageTitle("API Stress Test")
public class MainView extends VerticalLayout {
    // TODO can this class be cleaned?
    // TODO save parameters for re-use
    // TODO request preview (curl format?)
    // TODO enclose Run layout in card layout?

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
    private final Button startButton = new Button();
    private final Button stopButton = new Button();
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
        addEntry(headersLayout);

        uriVariablesLayout.setPadding(false);
        addEntry(uriVariablesLayout);

        queryParamsLayout.setPadding(false);
        addEntry(queryParamsLayout);

        requestBodyText.setPlaceholder("Request Body");
        requestBodyText.setWidth("60%");
        requestBody.setPadding(false);
        requestBody.add(requestBodyText);

        requestNumberField.setWidthFull();
        requestNumberField.setMin(1);
        requestNumberField.setValue(10);
        requestNumberField.addValueChangeListener(integerValidationListener(requestNumberField, 1));

        threadPoolSizeField.setWidthFull();
        threadPoolSizeField.setMin(1);
        threadPoolSizeField.setValue(12);
        threadPoolSizeField.addValueChangeListener(integerValidationListener(threadPoolSizeField, 1));

        startButton.addClickListener(event -> startStressTest());
        startButton.setIcon(PLAY.create());
        startButton.setWidth("18em");
        startButton.setHeight("2.5em");

        stopButton.addClickListener(event -> stopStressTest());
        stopButton.setVisible(false);
        stopButton.setIcon(STOP.create());
        stopButton.setWidth("18em");
        stopButton.setHeight("2.5em");

        stopOnError.setValue(true);

        completedText.setReadOnly(true);
        completedText.setWidth("8em");

        failedText.setReadOnly(true);
        failedText.setWidth("8em");

        errorText.setReadOnly(true);
        errorText.setWidthFull();

        var requestThreadLayout = new HorizontalLayout(requestNumberField, threadPoolSizeField);
        requestThreadLayout.setWidthFull();

        var resultsLayout = new HorizontalLayout(completedText, failedText, errorText);
        resultsLayout.setWidthFull();

        var runLayout = new VerticalLayout(
                new H3("Run"),
                requestThreadLayout,
                stopOnError,
                startButton, stopButton,
                resultsLayout);
        runLayout.setHorizontalComponentAlignment(CENTER, startButton, stopButton);
        runLayout.setPadding(false);
        runLayout.setMaxWidth("60%");

        var container = new VerticalLayout(
                new Headline(),
                urlLayout,
                new ToggleLayout("Headers", headersLayout),
                new ToggleLayout("URI Variables", uriVariablesLayout),
                new ToggleLayout("Query Parameters", queryParamsLayout),
                new ToggleLayout("Request Body", requestBody),
                runLayout);
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
                        headers, uriVariables, queryParams, requestBody,
                        result -> getUI().ifPresent(ui -> ui.access(() -> applyResponse(result)))
                ), newFixedThreadPool(threadPoolSize));
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
        errorText.setValue("");
    }

    private void applyResponse(ApiResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            completedRequests++;
            completedText.setValue(String.valueOf(completedRequests));
        } else {
            failedRequests++;
            failedText.setValue(String.valueOf(failedRequests));
            errorText.setValue(response.statusCode().value() + " - " + response.errorMessage());

            if (stopOnError.getValue()) {
                stopStressTest();
            }
        }

        // when stress test is completed, update UI to reflect it
        if (completedRequests + failedRequests == requestNumberField.getValue()) {
            stopStressTest();
        }
    }
}