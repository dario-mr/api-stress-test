package com.dario.ast.view.component.run;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.StressTestService;
import com.dario.ast.event.ApplyRunParamsEvent;
import com.dario.ast.proxy.ApiResponse;
import com.dario.ast.view.component.notification.WarnNotification;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Supplier;

import static com.dario.ast.util.IntegerFieldUtil.integerValidationListener;
import static com.vaadin.flow.component.icon.VaadinIcon.PLAY;
import static com.vaadin.flow.component.icon.VaadinIcon.STOP;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap.WRAP;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.springframework.util.StringUtils.hasText;

public class RunLayout extends VerticalLayout {

    private final StressTestService stressTestService;
    private final Supplier<ConfigParams> configParamsSupplier;

    private final IntegerField requestNumberField = new IntegerField("Requests");
    private final IntegerField threadPoolSizeField = new IntegerField("Threads");
    private final TextField completedText = new TextField("Completed");
    private final TextField failedText = new TextField("Failed");
    private final TextField errorText = new TextField("Error message");
    private final Button startButton = new Button();
    private final Button stopButton = new Button();
    private final Checkbox stopOnErrorCheckbox = new Checkbox("Stop on error");

    private long completedRequests = 0, failedRequests = 0;

    public RunLayout(StressTestService stressTestService, Supplier<ConfigParams> configParamsSupplier) {
        this.stressTestService = stressTestService;
        this.configParamsSupplier = configParamsSupplier;

        setWidthFull();
        addClassNames("card-layout", "run-layout");

        // requests + thread pool
        requestNumberField.setWidthFull();
        requestNumberField.setMinWidth("5em");
        requestNumberField.setMin(1);
        requestNumberField.addValueChangeListener(integerValidationListener(requestNumberField, 1));

        threadPoolSizeField.setWidthFull();
        threadPoolSizeField.setMinWidth("5em");
        threadPoolSizeField.setMin(1);
        threadPoolSizeField.addValueChangeListener(integerValidationListener(threadPoolSizeField, 1));

        var requestThreadLayout = new HorizontalLayout(requestNumberField, threadPoolSizeField);
        requestThreadLayout.setWidthFull();

        // start + stop buttons
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

        // add all components
        add(
                new H3("Run"),
                requestThreadLayout,
                stopOnErrorCheckbox,
                startButton, stopButton,
                resultsLayout
        );
        setHorizontalComponentAlignment(CENTER, startButton, stopButton);
    }

    public RunParams getRunParams() {
        var numRequests = requestNumberField.getValue();
        var threadPoolSize = threadPoolSizeField.getValue();
        var stopOnError = stopOnErrorCheckbox.getValue();

        return RunParams.builder()
                .numRequests(numRequests)
                .threadPoolSize(threadPoolSize)
                .stopOnError(stopOnError)
                .build();
    }

    private void applyParams(RunParams params) {
        requestNumberField.setValue(params.getNumRequests());
        threadPoolSizeField.setValue(params.getThreadPoolSize());
        stopOnErrorCheckbox.setValue(params.isStopOnError());
    }

    private void startStressTest() {
        // validate parameters
        var configParams = configParamsSupplier.get();
        if (!hasText(configParams.getUri())) {
            WarnNotification.show("Please provide a valid URI");
            return;
        }

        stopStressTest();
        startStressTestUI();

        var runParams = getRunParams();
        var astRequest = new AstRequest(configParams, runParams);
        var threadPoolSize = threadPoolSizeField.getValue();

        stressTestService.startStressTest(
                astRequest,
                response -> getUI().ifPresent(ui -> ui.access(() -> applyApiResponse(response))),
                newFixedThreadPool(threadPoolSize)
        );
    }

    private void stopStressTest() {
        stressTestService.cancelStressTest();

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

    private void applyApiResponse(ApiResponse response) {
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

        // when stress test is completed, update UI to reflect it (this is janky)
        if (completedRequests + failedRequests == requestNumberField.getValue()) {
            stopStressTest();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Listen for events that should trigger applying the run params in the UI
        ComponentUtil.addListener(attachEvent.getUI(),
                ApplyRunParamsEvent.class,
                event -> applyParams(event.getRunParams())
        );
    }
}
