package com.dario.ast.view.component.config;

import static com.dario.ast.core.domain.RequestHeader.defaultRequestHeader;
import static com.dario.ast.core.domain.RequestQueryParam.defaultRequestQueryParam;
import static com.dario.ast.core.domain.RequestUriVariable.defaultRequestUriVariable;
import static com.dario.ast.util.CurlPreviewUtil.buildCurlPreview;
import static com.dario.ast.util.MapUtil.removeGenericEmptyEntries;
import static com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap.WRAP;
import static org.springframework.http.HttpMethod.values;
import static org.springframework.util.StringUtils.hasText;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.core.domain.Request;
import com.dario.ast.core.domain.RequestHeader;
import com.dario.ast.core.domain.RequestQueryParam;
import com.dario.ast.core.domain.RequestType;
import com.dario.ast.core.domain.RequestUriVariable;
import com.dario.ast.core.service.RequestService;
import com.dario.ast.event.ConfigEntriesUpdatedEvent;
import com.dario.ast.event.FocusRequestNameEvent;
import com.dario.ast.util.EventUtil;
import com.dario.ast.view.component.common.ConfigTabs;
import com.dario.ast.view.component.common.entries.EntriesSection;
import com.dario.ast.view.component.common.notification.ErrorNotification;
import com.dario.ast.view.component.common.reactive.ReactiveComponent;
import com.dario.ast.view.component.common.reactive.ReactiveHandler;
import com.dario.ast.view.component.common.reactive.ReactiveType;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.vaadin.olli.ClipboardHelper;

@Slf4j
@UIScope
@SpringComponent
@CssImport(value = "./styles/config-layout.css")
@ReactiveComponent
public class ConfigLayout extends VerticalLayout {

  private final AppState appState;
  private final RequestService requestService;

  private final TextField nameText = new TextField();
  private final TextField urlText = new TextField();
  private final ComboBox<HttpMethod> methodCombo = new ComboBox<>(null, values());
  private final EntriesSection<RequestHeader> headerSection = new EntriesSection<>(
      EventUtil::configEntriesUpdated, RequestHeader::defaultRequestHeader);
  private final EntriesSection<RequestUriVariable> uriVariablesSection = new EntriesSection<>(
      EventUtil::configEntriesUpdated, RequestUriVariable::defaultRequestUriVariable);
  private final EntriesSection<RequestQueryParam> queryParamsSection = new EntriesSection<>(
      EventUtil::configEntriesUpdated, RequestQueryParam::defaultRequestQueryParam);
  private final TextArea requestBodyText = new TextArea();
  private final ClipboardHelper previewTextClipboard = new ClipboardHelper();
  private final TextArea previewText = new PreviewTextArea();

  private Long requestId;
  private Long userId;
  private RequestType requestType;
  private boolean active;

  private boolean isUiLoading = false;

  public ConfigLayout(AppState appState, RequestService requestService) {
    this.appState = appState;
    this.requestService = requestService;

    setWidthFull();
    setSpacing(false);
    addClassNames("card-layout", "config-layout");

    // name
    nameText.setPlaceholder("Request name");
    nameText.setWidthFull();
    nameText.setMinWidth("0");
    nameText.getStyle().set("padding-top", "var(--lumo-space-m)");

    // http method + url
    methodCombo.setMaxWidth("7.5em");
    urlText.setPlaceholder("Enter the URL");

    var urlMethodLayout = new FlexLayout(methodCombo, urlText);
    urlMethodLayout.setWidthFull();
    urlMethodLayout.setFlexWrap(WRAP);
    urlMethodLayout.setFlexGrow(1, urlText, methodCombo);
    urlMethodLayout.getStyle()
        .set("gap", "var(--lumo-space-s)");

    // config tabs
    var tabsMap = new LinkedHashMap<String, Component>();
    tabsMap.put("Headers", headerSection);
    tabsMap.put("URI Variables", uriVariablesSection);
    tabsMap.put("Query Parameters", queryParamsSection);
    tabsMap.put("Request Body", requestBodyText);

    // request body
    requestBodyText.setWidthFull();

    // curl preview
    previewTextClipboard.wrap(previewText);
    previewTextClipboard.getStyle().set("width", "100%");
    var curlPreviewToggleLayout = new ToggleLayout("cURL preview", previewTextClipboard);

    // config tabs
    var configTabs = new ConfigTabs(tabsMap);

    // add listeners
    addListeners();

    // add all components
    add(
        new H4("Configure"),
        nameText,
        urlMethodLayout,
        configTabs,
        curlPreviewToggleLayout
    );
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Listen for events indicating that config entries (EntriesSection class) were updated
    ComponentUtil.addListener(attachEvent.getUI(),
        ConfigEntriesUpdatedEvent.class,
        event -> saveParams()
    );

    // Listen for events indicating that the request name field should be focused
    ComponentUtil.addListener(attachEvent.getUI(),
        FocusRequestNameEvent.class,
        event -> nameText.focus()
    );
  }

  @ReactiveHandler(ReactiveType.REQUEST)
  public void onSelectedRequestChange(Request request) {
    loadConfigIntoUI(request.getConfigParams());
    generateCurlPreview();
  }

  @ReactiveHandler(ReactiveType.ENVIRONMENT)
  public void onEnvironmentChange(Environment environment) {
    generateCurlPreview();
  }

  private ConfigParams getConfigParams() {
    var name = nameText.getValue();
    var url = urlText.getValue();
    var method = methodCombo.getValue();
    var headers = removeGenericEmptyEntries(headerSection.getEntries());
    var uriVariables = removeGenericEmptyEntries(uriVariablesSection.getEntries());
    var queryParams = removeGenericEmptyEntries(queryParamsSection.getEntries());
    var requestBody = requestBodyText.getValue();

    return ConfigParams.builder()
        .requestId(requestId)
        .userId(userId)
        .requestType(requestType)
        .active(active)
        .requestName(name)
        .uri(url)
        .method(method)
        .headers(headers)
        .uriVariables(uriVariables)
        .queryParams(queryParams)
        .requestBody(requestBody)
        .build();
  }

  private void addListeners() {
    // name
    nameText.addValueChangeListener(event -> {
      if (isUiLoading) {
        return;
      }

      var oldValue = event.getOldValue();
      var newValue = event.getValue();
      if (!hasText(newValue)) {
        nameText.setValue(oldValue);
        return;
      }
      if (!newValue.equals(oldValue)) {
        saveParams();
      }
    });

    // url
    urlText.addValueChangeListener(event -> {
      if (isUiLoading) {
        return;
      }

      var oldValue = event.getOldValue();
      var newValue = event.getValue();
      if (!hasText(newValue)) {
        urlText.setValue(oldValue);
        return;
      }
      if (!newValue.equals(oldValue)) {
        saveParams();
      }
    });

    // request body
    requestBodyText.addValueChangeListener(event -> {
      if (isUiLoading) {
        return;
      }

      var oldValue = event.getOldValue();
      var newValue = event.getValue();

      if (!newValue.equals(oldValue)) {
        saveParams();
      }
    });

    // method
    methodCombo.addValueChangeListener(event -> {
      if (isUiLoading) {
        return;
      }

      var oldValue = event.getOldValue();
      var newValue = event.getValue();
      if (newValue == null) {
        methodCombo.setValue(oldValue);
        return;
      }
      if (!newValue.equals(oldValue)) {
        saveParams();
      }
    });
  }

  private void saveParams() {
    var configParams = getConfigParams();

    try {
      requestService.updateConfigParams(configParams);
    } catch (Exception ex) {
      ErrorNotification.show("Error saving parameters");
      throw ex;
    }

    var selectedRequest = appState.getSelectedRequest();
    selectedRequest.setConfigParams(configParams);
    appState.setSelectedRequest(selectedRequest);
  }

  private void generateCurlPreview() {
    var configParams = getConfigParams();
    var selectedEnvironment = appState.getSelectedEnvironment();
    var curlPreview = buildCurlPreview(configParams, selectedEnvironment);

    previewText.setValue(curlPreview);
    previewTextClipboard.setContent(curlPreview); // prepare curl preview to be copied to user's clipboard

    log.debug("cURL preview regenerated");
  }

  private void loadConfigIntoUI(ConfigParams params) {
    isUiLoading = true;

    this.requestId = params.getRequestId();
    this.userId = params.getUserId();
    this.requestType = params.getRequestType();
    this.active = params.isActive();

    nameText.setValue(params.getRequestName());

    urlText.setValue(params.getUri());
    methodCombo.setValue(params.getMethod());

    headerSection.clearEntries();
    headerSection.addEntries(params.getHeaders());
    headerSection.addEntry("", defaultRequestHeader());

    uriVariablesSection.clearEntries();
    uriVariablesSection.addEntries(params.getUriVariables());
    uriVariablesSection.addEntry("", defaultRequestUriVariable());

    queryParamsSection.clearEntries();
    queryParamsSection.addEntries(params.getQueryParams());
    queryParamsSection.addEntry("", defaultRequestQueryParam());

    requestBodyText.setValue(params.getRequestBody() == null
        ? "" : params.getRequestBody());

    isUiLoading = false;

    log.debug("Config params [{}] loaded into {}", params.getRequestId(), getClass().getSimpleName());
  }

}
