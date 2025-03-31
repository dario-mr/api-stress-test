package com.dario.ast.view.component.run;

import static com.dario.ast.util.EnvironmentUtil.applyEnvironmentVariables;
import static com.dario.ast.util.IntegerFieldUtil.integerValidationListener;
import static com.vaadin.flow.component.icon.VaadinIcon.PLAY;
import static com.vaadin.flow.component.icon.VaadinIcon.STOP;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap.WRAP;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.springframework.util.StringUtils.hasText;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.core.service.StressTestService;
import com.dario.ast.proxy.ApiResponse;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.dario.ast.view.component.common.notification.WarnNotification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UIScope
@SpringComponent
@CssImport(value = "./styles/run-layout.css")
public class RunLayout extends VerticalLayout {

  private final StressTestService stressTestService;
  private final AppState appState;
  private final AstRequestService astRequestService;

  private final IntegerField requestNumberField = new IntegerField("Requests");
  private final IntegerField threadPoolSizeField = new IntegerField("Threads");
  private final TextField completedText = new TextField("Completed");
  private final TextField failedText = new TextField("Failed");
  private final TextField errorText = new TextField("Error message");
  private final Button startButton = new Button();
  private final Button stopButton = new Button();
  private final Checkbox stopOnErrorCheckbox = new Checkbox("Stop on error");

  private long completedRequests = 0, failedRequests = 0;
  private Long requestId;

  public RunLayout(
      StressTestService stressTestService,
      AppState appState,
      AstRequestService astRequestService) {
    this.stressTestService = stressTestService;
    this.appState = appState;
    this.astRequestService = astRequestService;

    setWidthFull();
    setSpacing(false);
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

    // add listeners
    addBlurListeners();
    appState.getRunParamsStream().subscribe(runParams ->
        UI.getCurrent().access(() -> applyParams(runParams))
    );

    // add all components
    add(
        new H4("Run"),
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
        .requestId(requestId)
        .numRequests(numRequests)
        .threadPoolSize(threadPoolSize)
        .stopOnError(stopOnError)
        .build();
  }

  private void addBlurListeners() {
    // name
    requestNumberField.addBlurListener(event -> {
      var currentValue = appState.getRunParams().getNumRequests();
      var newValue = requestNumberField.getValue();
      if (newValue != null && newValue > 0 && !newValue.equals(currentValue)) {
        saveParams();
      }
    });

    // url
    threadPoolSizeField.addBlurListener(event -> {
      var currentValue = appState.getRunParams().getThreadPoolSize();
      var newValue = threadPoolSizeField.getValue();
      if (newValue != null && newValue > 0 && !newValue.equals(currentValue)) {
        saveParams();
      }
    });

    // request
    stopOnErrorCheckbox.addClickListener(event -> {
      var currentValue = appState.getRunParams().isStopOnError();
      var newValue = stopOnErrorCheckbox.getValue();
      if (newValue != null && !newValue.equals(currentValue)) {
        saveParams();
      }
    });
  }

  private void saveParams() {
    var configParams = appState.getConfigParams();
    var runParams = getRunParams();

    try {
      astRequestService.update(new AstRequest(configParams, runParams));
    } catch (Exception ex) {
      ErrorNotification.show("Error saving parameters");
      throw ex;
    }

    appState.setRunParams(runParams);
  }

  private void applyParams(RunParams params) {
    this.requestId = params.getRequestId();

    requestNumberField.setValue(params.getNumRequests());
    threadPoolSizeField.setValue(params.getThreadPoolSize());
    stopOnErrorCheckbox.setValue(params.isStopOnError());
  }

  private void startStressTest() {
    // validate parameters
    var configParams = appState.getConfigParams();
    if (!hasText(configParams.getUri())) {
      WarnNotification.show("Please provide a valid URL");
      return;
    }

    stopStressTest();
    startStressTestUI();

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

}
