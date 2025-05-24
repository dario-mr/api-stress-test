package com.dario.ast.view.component.run;

import static com.dario.ast.util.IntegerFieldUtil.integerValidationListener;
import static com.dario.ast.util.JsonUtil.prettifyJson;
import static com.vaadin.flow.component.icon.VaadinIcon.PLAY;
import static com.vaadin.flow.component.icon.VaadinIcon.STOP;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END;
import static com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap.WRAP;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.Request;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.RequestService;
import com.dario.ast.core.service.RequestValidationService;
import com.dario.ast.core.service.StressTestOrchestrator;
import com.dario.ast.proxy.api.dto.ApiResponse;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.dario.ast.view.component.common.notification.WarnNotification;
import com.dario.ast.view.component.common.reactive.ReactiveComponent;
import com.dario.ast.view.component.common.reactive.ReactiveHandler;
import com.dario.ast.view.component.common.reactive.ReactiveType;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UIScope
@SpringComponent
@CssImport(value = "./styles/run-layout.css")
@ReactiveComponent
public class RunLayout extends VerticalLayout {

  private static final String RESPONSE_LABEL = "Response";

  private final AppState appState;
  private final RequestService requestService;
  private final RequestValidationService validationService;
  private final StressTestOrchestrator stressTestOrchestrator;

  private final IntegerField requestNumberField = new IntegerField("Requests");
  private final IntegerField threadPoolSizeField = new IntegerField("Threads");
  private final TextField completedText = new TextField("Completed");
  private final TextField failedText = new TextField("Failed");
  private final TextArea responseText = new TextArea(RESPONSE_LABEL);
  private final Button startButton = new Button();
  private final Button stopButton = new Button();
  private final Checkbox stopOnErrorCheckbox = new Checkbox("Stop on error");
  private final ProgressBar progressBar = new ProgressBar();

  private long completedRequests = 0, failedRequests = 0;
  private Long requestId;
  private boolean isUiLoading = false;

  public RunLayout(
      AppState appState,
      RequestService requestService,
      RequestValidationService validationService,
      StressTestOrchestrator stressTestOrchestrator) {
    this.appState = appState;
    this.requestService = requestService;
    this.validationService = validationService;
    this.stressTestOrchestrator = stressTestOrchestrator;

    addClassNames("card-layout", "run-layout");
    setWidthFull();
    setSpacing(false);

    // requests + thread pool + stop on error
    requestNumberField.setWidthFull();
    requestNumberField.setMinWidth("5em");
    requestNumberField.setMin(1);
    requestNumberField.addValueChangeListener(integerValidationListener(requestNumberField, 1));

    threadPoolSizeField.setWidthFull();
    threadPoolSizeField.setMinWidth("5em");
    threadPoolSizeField.setMin(1);
    threadPoolSizeField.addValueChangeListener(integerValidationListener(threadPoolSizeField, 1));

    stopOnErrorCheckbox.setMinWidth("9em");

    var firstRow = new HorizontalLayout(requestNumberField, threadPoolSizeField, stopOnErrorCheckbox);
    firstRow.setVerticalComponentAlignment(END, stopOnErrorCheckbox);
    firstRow.setWidthFull();

    // progress bar
    progressBar.setWidthFull();
    progressBar.setMin(0);
    progressBar.setMax(1);
    progressBar.addClassName("progress-bar");

    // start + stop buttons
    startButton.addClickListener(event -> startStressTest());
    startButton.setIcon(PLAY.create());
    startButton.addClassName("start-stop-button");

    stopButton.addClickListener(event -> {
      stopStressTest();
      log.debug("Stress Test cancelled by the user");
    });
    stopButton.setIcon(STOP.create());
    stopButton.setVisible(false);
    stopButton.addClassName("start-stop-button");

    // result layout (completed, failed, errors)
    completedText.setReadOnly(true);
    completedText.setWidth("8em");

    failedText.setReadOnly(true);
    failedText.setWidth("8em");

    responseText.setReadOnly(true);
    responseText.setWidthFull();

    var resultsLayout = new FlexLayout(completedText, failedText, responseText);
    resultsLayout.setWidthFull();
    resultsLayout.setFlexWrap(WRAP);
    resultsLayout.setFlexGrow(1, completedText, failedText);
    resultsLayout.getStyle().set("gap", "var(--lumo-space-m)");

    // add listeners
    addListeners();

    // add all components
    add(
        new H4("Run"),
        firstRow,
        startButton, stopButton,
        progressBar,
        resultsLayout
    );
    setHorizontalComponentAlignment(CENTER, startButton, stopButton);
  }

  private RunParams getRunParams() {
    var numRequests = requestNumberField.getValue();
    var threadPoolSize = threadPoolSizeField.getValue();
    var stopOnError = stopOnErrorCheckbox.getValue();

    return RunParams.builder()
        .requestId(requestId)
        .numRequests(numRequests)
        .threadPoolSize(threadPoolSize)
        .stopOnError(stopOnError)
        .build();
  }

  @ReactiveHandler(ReactiveType.REQUEST)
  public void onSelectedRequestChange(Request request) {
    loadRunParamsIntoUi(request.getRunParams());
  }

  private void addListeners() {
    // name
    requestNumberField.addValueChangeListener(event -> {
      if (isUiLoading) {
        return;
      }

      var oldValue = event.getOldValue();
      var newValue = event.getValue();
      if (newValue != null && newValue > 0 && !newValue.equals(oldValue)) {
        saveParams();
      }
    });

    // url
    threadPoolSizeField.addValueChangeListener(event -> {
      if (isUiLoading) {
        return;
      }

      var oldValue = event.getOldValue();
      var newValue = event.getValue();
      if (newValue != null && newValue > 0 && !newValue.equals(oldValue)) {
        saveParams();
      }
    });

    // request
    stopOnErrorCheckbox.addValueChangeListener(event -> {
      if (isUiLoading) {
        return;
      }

      var oldValue = event.getOldValue();
      var newValue = event.getValue();
      if (newValue != null && !newValue.equals(oldValue)) {
        saveParams();
      }
    });
  }

  private void saveParams() {
    var runParams = getRunParams();

    try {
      requestService.updateRunParams(runParams);
    } catch (Exception ex) {
      ErrorNotification.show("Error saving parameters");
      throw ex;
    }

    var selectedRequest = appState.getSelectedRequest();
    selectedRequest.setRunParams(runParams);
    appState.setSelectedRequest(selectedRequest);
  }

  private void loadRunParamsIntoUi(RunParams params) {
    isUiLoading = true;
    this.requestId = params.getRequestId();

    requestNumberField.setValue(params.getNumRequests());
    threadPoolSizeField.setValue(params.getThreadPoolSize());
    stopOnErrorCheckbox.setValue(params.isStopOnError());
    progressBar.setValue(0);

    isUiLoading = false;
    log.debug("Run params [{}] loaded into {}", params.getRequestId(), getClass().getSimpleName());
  }

  private void startStressTest() {
    var configParams = appState.getSelectedRequest().getConfigParams();

    var validationResult = validationService.validate(configParams);
    if (!validationResult.success()) {
      WarnNotification.show(validationResult.message());
      return;
    }

    log.debug("Starting stress test");

    stopStressTest();
    startStressTestUI();

    stressTestOrchestrator.startStressTest(
        configParams,
        getRunParams(),
        response -> getUI().ifPresent(ui -> ui.access(() -> applyApiResponse(response))),
        () -> getUI().ifPresent(ui -> ui.access(() -> {
          log.debug("Stress Test completed");
          onStressTestComplete();
        })),
        ex -> getUI().ifPresent(ui -> ui.access(() -> {
          log.error("Error during Stress Test", ex);
          onStressTestComplete();
          responseText.setValue(ex.getMessage());

          ErrorNotification.show("An error occurred");
        }))
    );
  }

  private void stopStressTest() {
    stressTestOrchestrator.cancelStressTest();
    onStressTestComplete();
  }

  private void onStressTestComplete() {
    requestNumberField.setEnabled(true);
    threadPoolSizeField.setEnabled(true);
    stopOnErrorCheckbox.setEnabled(true);
    startButton.setVisible(true);
    stopButton.setVisible(false);
  }

  private void startStressTestUI() {
    completedRequests = 0;
    failedRequests = 0;

    requestNumberField.setEnabled(false);
    threadPoolSizeField.setEnabled(false);
    stopOnErrorCheckbox.setEnabled(false);
    startButton.setVisible(false);
    stopButton.setVisible(true);

    completedText.clear();
    failedText.clear();
    responseText.clear();
    responseText.setLabel(RESPONSE_LABEL);
    progressBar.setValue(0);
  }

  private void applyApiResponse(ApiResponse response) {
    responseText.setLabel("%s (%s)".formatted(RESPONSE_LABEL, response.statusCode()));

    if (response.statusCode().is2xxSuccessful()) {
      applySuccessResponse(response);
    } else {
      applyFailResponse(response);

      if (stopOnErrorCheckbox.getValue()) {
        log.debug("Stress Test stopped due to error: {}", response.statusCode());
        stopStressTest();
        return;
      }
    }

    updateProgressBar();
  }

  private void applySuccessResponse(ApiResponse response) {
    completedRequests++;
    completedText.setValue(String.valueOf(completedRequests));
    responseText.setValue(prettifyJson(response.responseBody()));
  }

  private void applyFailResponse(ApiResponse response) {
    failedRequests++;
    failedText.setValue(String.valueOf(failedRequests));
    responseText.setValue(prettifyJson(response.errorMessage()));
  }

  private void updateProgressBar() {
    var progress = (double) (completedRequests + failedRequests) / requestNumberField.getValue();
    progressBar.setValue(Math.min(progress, 1.0));
  }

}
