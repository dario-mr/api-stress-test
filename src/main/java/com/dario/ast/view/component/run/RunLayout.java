package com.dario.ast.view.component.run;

import static com.dario.ast.util.EnvironmentUtil.applyEnvironmentVariables;
import static com.dario.ast.util.IntegerFieldUtil.integerValidationListener;
import static com.vaadin.flow.component.icon.VaadinIcon.PLAY;
import static com.vaadin.flow.component.icon.VaadinIcon.STOP;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END;
import static com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap.WRAP;
import static java.util.concurrent.Executors.newFixedThreadPool;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.core.service.PreRequestService;
import com.dario.ast.core.service.RequestValidationService;
import com.dario.ast.core.service.StressTestService;
import com.dario.ast.proxy.api.dto.ApiResponse;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.dario.ast.view.component.common.notification.WarnNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
public class RunLayout extends VerticalLayout {

  private static final ObjectMapper JSON_FORMATTER = new ObjectMapper()
      .enable(SerializationFeature.INDENT_OUTPUT);
  private static final String RESPONSE_LABEL = "Response";

  private final StressTestService stressTestService;
  private final AppState appState;
  private final AstRequestService astRequestService;
  private final PreRequestService prerequestService;
  private final RequestValidationService validationService;

  private final IntegerField requestNumberField = new IntegerField("Requests");
  private final IntegerField threadPoolSizeField = new IntegerField("Threads");
  private final TextField completedText = new TextField("Completed");
  private final TextField failedText = new TextField("Failed");
  private final TextArea responseText = new TextArea(RESPONSE_LABEL);
  private final Button startButton = new Button();
  private final Button stopButton = new Button();
  private final Checkbox stopOnErrorCheckbox = new Checkbox("Stop on error");

  private long completedRequests = 0, failedRequests = 0;
  private Long requestId;
  private boolean isUiLoading = false;

  public RunLayout(
      StressTestService stressTestService,
      AppState appState,
      AstRequestService astRequestService,
      PreRequestService prerequestService,
      RequestValidationService validationService) {
    this.stressTestService = stressTestService;
    this.appState = appState;
    this.astRequestService = astRequestService;
    this.prerequestService = prerequestService;
    this.validationService = validationService;

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

    responseText.setReadOnly(true);
    responseText.setWidthFull();

    var resultsLayout = new FlexLayout(completedText, failedText, responseText);
    resultsLayout.setWidthFull();
    resultsLayout.setFlexWrap(WRAP);
    resultsLayout.setFlexGrow(1, completedText, failedText);
    resultsLayout.getStyle().set("gap", "var(--lumo-space-m)");

    // add listeners
    addListeners();
    observeAppState();

    // add all components
    add(
        new H4("Run"),
        firstRow,
        startButton, stopButton,
        resultsLayout,
        responseText
    );
    setHorizontalComponentAlignment(CENTER, startButton, stopButton);
  }

  public RunParams getRunParams() {
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

  private void observeAppState() {
    appState.getRunParamsStream().subscribe(this::loadRunParamsIntoUi);
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
      astRequestService.updateRunParams(runParams);
    } catch (Exception ex) {
      ErrorNotification.show("Error saving parameters");
      throw ex;
    }

    appState.setRunParams(runParams);
  }

  private void loadRunParamsIntoUi(RunParams params) {
    isUiLoading = true;
    this.requestId = params.getRequestId();

    requestNumberField.setValue(params.getNumRequests());
    threadPoolSizeField.setValue(params.getThreadPoolSize());
    stopOnErrorCheckbox.setValue(params.isStopOnError());

    isUiLoading = false;
    log.debug("Run params [{}] loaded into {}", params.getRequestId(), getClass().getSimpleName());
  }

  private void startStressTest() {
    var configParams = appState.getConfigParams();

    var validationResult = validationService.validate(configParams);
    if (!validationResult.success()) {
      WarnNotification.show(validationResult.message());
      return;
    }

    log.debug("Starting requests");
    stopStressTest();
    startStressTestUI();

    prerequestService.runAndApplyPreRequests();

    var selectedEnvironment = appState.getSelectedEnvironment();
    var envConfigParams = applyEnvironmentVariables(configParams, selectedEnvironment);
    var runParams = getRunParams();
    var threadPoolSize = threadPoolSizeField.getValue();

    stressTestService.startStressTest(
        envConfigParams, runParams,
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
    responseText.clear();
    responseText.setLabel(RESPONSE_LABEL);
  }

  private void applyApiResponse(ApiResponse response) {
    responseText.setLabel("%s (%s)".formatted(RESPONSE_LABEL, response.statusCode()));

    if (response.statusCode().is2xxSuccessful()) {
      completedRequests++;
      completedText.setValue(String.valueOf(completedRequests));
      responseText.setValue(formatJson(response.responseBody()));
    } else {
      failedRequests++;
      failedText.setValue(String.valueOf(failedRequests));
      responseText.setValue(response.errorMessage());

      if (stopOnErrorCheckbox.getValue()) {
        log.debug("Request [{}] failed", requestId);
        stopStressTest();
      }
    }

    // when stress test is completed, update UI to reflect it (this is janky)
    if (completedRequests + failedRequests == requestNumberField.getValue()) {
      log.debug("Requests completed");
      stopStressTest();
    }
  }

  private String formatJson(String json) {
    try {
      Object jsonObject = JSON_FORMATTER.readValue(json, Object.class);
      return JSON_FORMATTER.writeValueAsString(jsonObject);
    } catch (Exception e) {
      return json;
    }
  }

}
