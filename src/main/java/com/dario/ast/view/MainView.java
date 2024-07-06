package com.dario.ast.view;

import com.dario.ast.core.domain.StressTestParams;
import com.dario.ast.core.service.ParamService;
import com.dario.ast.core.service.StressService;
import com.dario.ast.event.RefreshPreviewEvent;
import com.dario.ast.proxy.ApiResponse;
import com.dario.ast.view.component.*;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.vaadin.olli.ClipboardHelper;

import static com.dario.ast.util.EventUtil.refreshPreview;
import static com.dario.ast.util.IntegerFieldUtil.integerValidationListener;
import static com.dario.ast.util.MapUtil.removeEmptyEntries;
import static com.vaadin.flow.component.icon.VaadinIcon.PLAY;
import static com.vaadin.flow.component.icon.VaadinIcon.STOP;
import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN;
import static com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap.WRAP;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.springframework.http.HttpMethod.values;

@Route
@Slf4j
@RequiredArgsConstructor
@PageTitle("API Stress Test")
public class MainView extends VerticalLayout {

    // TODO validation when clicking start
    // TODO results history?
    // TODO persist parameters to db?

    private final static String MAX_WINDOW_WIDTH = "1000px";

    private final StressService stressService;
    private final ParamService paramService;

    private final TextField urlText = new TextField("API endpoint");
    private final ComboBox<HttpMethod> methodCombo = new ComboBox<>("Method", values());
    private final EntriesSection headerSection = new EntriesSection();
    private final EntriesSection uriVariablesSection = new EntriesSection();
    private final EntriesSection queryParamsSection = new EntriesSection();
    private final TextArea requestBodyText = new TextArea();
    private final ClipboardHelper previewTextClipboard = new ClipboardHelper();
    private final TextArea previewText = new PreviewTextArea();
    private final IntegerField requestNumberField = new IntegerField("Requests");
    private final IntegerField threadPoolSizeField = new IntegerField("Threads");
    private final TextField completedText = new TextField("Completed");
    private final TextField failedText = new TextField("Failed");
    private final TextField errorText = new TextField("Error message");
    private final Button startButton = new Button();
    private final Button stopButton = new Button();
    private final Checkbox stopOnErrorCheckbox = new Checkbox("Stop on error");

    private long completedRequests = 0, failedRequests = 0;

    @PostConstruct
    public void init() {
        // headline
        var headlineLayout = new HorizontalLayout(
                new Headline(),
                new SaveButton(paramService, empty -> getStressTestParams()));
        headlineLayout.setWidthFull();
        headlineLayout.setAlignItems(CENTER);
        headlineLayout.setJustifyContentMode(BETWEEN);

        // url + http method
        urlText.setWidth("20em");
        methodCombo.setMaxWidth("7.5em");

        var urlLayout = new FlexLayout(urlText, methodCombo);
        urlLayout.setWidthFull();
        urlLayout.setFlexWrap(WRAP);
        urlLayout.setFlexGrow(1, urlText, methodCombo);
        urlLayout.getStyle()
                .set("margin-bottom", "1em")
                .set("gap", "var(--lumo-space-m)");

        // request body
        requestBodyText.setWidthFull();

        // requests number
        requestNumberField.setWidthFull();
        requestNumberField.setMinWidth("5em");
        requestNumberField.setMin(1);
        requestNumberField.addValueChangeListener(integerValidationListener(requestNumberField, 1));

        // thread pool size
        threadPoolSizeField.setWidthFull();
        threadPoolSizeField.setMinWidth("5em");
        threadPoolSizeField.setMin(1);
        threadPoolSizeField.addValueChangeListener(integerValidationListener(threadPoolSizeField, 1));

        var requestThreadLayout = new HorizontalLayout(requestNumberField, threadPoolSizeField);
        requestThreadLayout.setWidthFull();

        // start / stop button
        startButton.addClickListener(event -> startStressTest());
        startButton.setIcon(PLAY.create());
        startButton.addClassName("start-stop-button");

        stopButton.addClickListener(event -> stopStressTest());
        stopButton.setIcon(STOP.create());
        stopButton.setVisible(false);
        stopButton.addClassName("start-stop-button");

        // result layout (completed, failed, errors)
        completedText.setReadOnly(true);
        completedText.setWidth("8em");

        failedText.setReadOnly(true);
        failedText.setWidth("8em");

        errorText.setReadOnly(true);
        errorText.setWidth("20em");

        var resultsLayout = new FlexLayout(completedText, failedText, errorText);
        resultsLayout.setWidthFull();
        resultsLayout.setFlexWrap(WRAP);
        resultsLayout.setFlexGrow(1, completedText, failedText);
        resultsLayout.setFlexGrow(1, errorText);
        resultsLayout.getStyle().set("gap", "var(--lumo-space-m)");

        // preview
        previewTextClipboard.wrap(previewText);
        previewTextClipboard.getStyle().set("width", "100%");

        // run layout
        var runLayout = new VerticalLayout(
                new H3("Run"),
                requestThreadLayout,
                stopOnErrorCheckbox,
                startButton, stopButton,
                resultsLayout);
        runLayout.setHorizontalComponentAlignment(CENTER, startButton, stopButton);
        runLayout.addClassName("card-layout");
        runLayout.setWidthFull();

        // container with everything
        var container = new VerticalLayout(
                headlineLayout,
                urlLayout,
                new ToggleLayout("Headers", headerSection),
                new ToggleLayout("URI Variables", uriVariablesSection),
                new ToggleLayout("Query Parameters", queryParamsSection),
                new ToggleLayout("Request Body", requestBodyText),
                previewTextClipboard,
                runLayout);
        container.setMaxWidth(MAX_WINDOW_WIDTH);

        add(container);
        setAlignItems(CENTER);
        setPadding(false);

        getAndApplyParams();
    }

    private void getAndApplyParams() {
        paramService.getParams()
                .thenAccept(this::applyParams)
                .exceptionally(ex -> {
                    Notification.show("Error loading parameters", 2_000, TOP_CENTER).addThemeVariants(LUMO_ERROR);
                    log.error("Error loading parameters: {}", ex.getMessage());

                    return null;
                })
                .thenAccept(unused -> generateCurlPreview())
                .thenAccept(unused -> addValueChangeListeners());
    }

    private void generateCurlPreview() {
        var params = getStressTestParams();

        // start with "curl -X"
        var curlPreviewBuilder = new StringBuilder("curl -X");

        // replace uri variables in uri
        var uri = params.getUri();
        for (var uriVar : params.getUriVariables().entrySet()) {
            uri = uri.replace("{" + uriVar.getKey() + "}", uriVar.getValue());
        }

        // append http method
        curlPreviewBuilder.append(" ").append(params.getMethod().name());

        // append uri
        curlPreviewBuilder.append(" '").append(uri);

        // append query parameters
        boolean isFirstQueryParam = true;
        for (var queryParam : params.getQueryParams().entrySet()) {
            if (isFirstQueryParam) {
                curlPreviewBuilder.append("?");
                isFirstQueryParam = false;
            } else {
                curlPreviewBuilder.append("&");
            }
            curlPreviewBuilder.append(queryParam.getKey()).append("=").append(queryParam.getValue());
        }
        curlPreviewBuilder.append("' \\\n");

        // append headers
        for (var header : params.getHeaders().entrySet()) {
            curlPreviewBuilder.append(" -H '")
                    .append(header.getKey()).append(": ").append(header.getValue())
                    .append("' \\\n");
        }

        // append request body
        if (!params.getRequestBody().isEmpty()) {
            curlPreviewBuilder.append(" -d '").append(params.getRequestBody()).append("'");
        }

        var curlPreview = curlPreviewBuilder.toString();
        previewText.setValue(curlPreview);

        // prepare curl preview to be copied to user's clipboard
        previewTextClipboard.setContent(curlPreview);
    }

    private void addValueChangeListeners() {
        urlText.addValueChangeListener(event -> refreshPreview());
        methodCombo.addValueChangeListener(event -> refreshPreview());
        requestBodyText.addValueChangeListener(event -> refreshPreview());
    }

    private void applyParams(StressTestParams params) {
        urlText.setValue(params.getUri());
        methodCombo.setValue(params.getMethod());

        headerSection.addEntries(params.getHeaders());
        headerSection.addEntry("", "");

        uriVariablesSection.addEntries(params.getUriVariables());
        uriVariablesSection.addEntry("", "");

        queryParamsSection.addEntries(params.getQueryParams());
        queryParamsSection.addEntry("", "");

        requestBodyText.setValue(params.getRequestBody());

        requestNumberField.setValue(params.getNumRequests());
        threadPoolSizeField.setValue(params.getThreadPoolSize());

        stopOnErrorCheckbox.setValue(params.isStopOnError());
    }

    private StressTestParams getStressTestParams() {
        var numRequests = requestNumberField.getValue();
        var threadPoolSize = threadPoolSizeField.getValue();
        var url = urlText.getValue();
        var method = methodCombo.getValue();
        var headers = removeEmptyEntries(headerSection.getEntries());
        var uriVariables = removeEmptyEntries(uriVariablesSection.getEntries());
        var queryParams = removeEmptyEntries(queryParamsSection.getEntries());
        var requestBody = requestBodyText.getValue();
        var stopOnError = stopOnErrorCheckbox.getValue();

        return new StressTestParams(
                numRequests, threadPoolSize,
                url, method,
                headers, uriVariables, queryParams, requestBody, stopOnError
        );
    }

    private void startStressTest() {
        stopStressTest();
        startStressTestUI();

        var threadPoolSize = threadPoolSizeField.getValue();

        stressService.startStressTest(
                getStressTestParams(),
                response -> getUI().ifPresent(ui -> ui.access(() -> applyResponse(response))),
                newFixedThreadPool(threadPoolSize)
        );
    }

    private void stopStressTest() {
        stressService.cancelStressTest();

        completedRequests = 0;
        failedRequests = 0;

        requestNumberField.setEnabled(true);
        threadPoolSizeField.setEnabled(true);
        stopOnErrorCheckbox.setEnabled(true);
        startButton.setVisible(true);
        stopButton.setVisible(false);
    }

    private void startStressTestUI() {
        requestNumberField.setEnabled(false);
        threadPoolSizeField.setEnabled(false);
        stopOnErrorCheckbox.setEnabled(false);
        startButton.setVisible(false);
        stopButton.setVisible(true);

        completedText.clear();
        failedText.clear();
        errorText.clear();
    }

    private void applyResponse(ApiResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            completedRequests++;
            completedText.setValue(String.valueOf(completedRequests));
        } else {
            failedRequests++;
            failedText.setValue(String.valueOf(failedRequests));
            errorText.setValue(response.statusCode().value() + " - " + response.errorMessage());

            if (stopOnErrorCheckbox.getValue()) {
                stopStressTest();
            }
        }

        // when stress test is completed, update UI to reflect it
        if (completedRequests + failedRequests == requestNumberField.getValue()) {
            stopStressTest();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Listen for events that should trigger preview refresh
        ComponentUtil.addListener(
                attachEvent.getUI(),
                RefreshPreviewEvent.class,
                event -> generateCurlPreview()
        );
    }
}