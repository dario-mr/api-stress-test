package com.dario.ast.view.component.prerequest;

import static com.dario.ast.core.domain.RequestHeader.defaultRequestHeader;
import static com.dario.ast.core.domain.RequestQueryParam.defaultRequestQueryParam;
import static com.dario.ast.core.domain.RequestUriVariable.defaultRequestUriVariable;
import static com.dario.ast.util.EventUtil.preRequestUpdated;
import static com.dario.ast.util.MapUtil.removeGenericEmptyEntries;
import static com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap.WRAP;
import static org.springframework.http.HttpMethod.values;
import static org.springframework.util.StringUtils.hasText;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RequestHeader;
import com.dario.ast.core.domain.RequestQueryParam;
import com.dario.ast.core.domain.RequestType;
import com.dario.ast.core.domain.RequestUriVariable;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.event.ApplyPreRequestEvent;
import com.dario.ast.event.FocusPreRequestNameEvent;
import com.dario.ast.event.PreRequestConfigEntriesUpdatedEvent;
import com.dario.ast.util.EventUtil;
import com.dario.ast.view.component.common.ConfigTabs;
import com.dario.ast.view.component.common.entries.EntriesSection;
import com.dario.ast.view.component.common.notification.ErrorNotification;
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
import org.springframework.http.HttpMethod;

@UIScope
@SpringComponent
@CssImport(value = "./styles/pre-request-layout.css")
public class PreRequestLayout extends VerticalLayout {

  private final AppState appState;
  private final AstRequestService astRequestService;

  private final TextField nameText = new TextField();
  private final TextField urlText = new TextField();
  private final ComboBox<HttpMethod> methodCombo = new ComboBox<>(null, values());
  private final EntriesSection<RequestHeader> headerSection = new EntriesSection<>(
      EventUtil::preRequestConfigEntriesUpdated, RequestHeader::defaultRequestHeader);
  private final EntriesSection<RequestUriVariable> uriVariablesSection = new EntriesSection<>(
      EventUtil::preRequestConfigEntriesUpdated, RequestUriVariable::defaultRequestUriVariable);
  private final EntriesSection<RequestQueryParam> queryParamsSection = new EntriesSection<>(
      EventUtil::preRequestConfigEntriesUpdated, RequestQueryParam::defaultRequestQueryParam);
  private final TextArea requestBodyText = new TextArea();

  private Long requestId;
  private Long userId;
  private RequestType requestType;
  private boolean active;
  private RunParams runParams;

  private boolean isUiLoading = false;

  // TODO store pre-requests in AppState? use observer pattern
  public PreRequestLayout(AppState appState, AstRequestService astRequestService) {
    this.appState = appState;
    this.astRequestService = astRequestService;

    addClassNames("card-layout", "pre-request-layout");
    setSpacing(false);
    setWidthFull();

    // name
    nameText.setPlaceholder("Pre-request name, used as environment variable");
    nameText.setWidthFull();
    nameText.setMaxWidth("30em");
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
    requestBodyText.getStyle().set("font-family", "monospace");

    // add listeners
    addListeners();

    add(
        new H4("Pre-request"),
        nameText,
        urlMethodLayout,
        new ConfigTabs(tabsMap)
    );
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    // Listen for events that should trigger applying the given pre-request in the UI
    ComponentUtil.addListener(attachEvent.getUI(),
        ApplyPreRequestEvent.class,
        event -> loadPreRequestIntoUI(event.getPreRequest())
    );

    // Listen for events indicating that the request name field should be focused
    ComponentUtil.addListener(attachEvent.getUI(),
        FocusPreRequestNameEvent.class,
        event -> nameText.focus()
    );

    // Listen for events indicating that config entries (EntriesSection class) were updated
    ComponentUtil.addListener(attachEvent.getUI(),
        PreRequestConfigEntriesUpdatedEvent.class,
        event -> saveParams()
    );
  }

  private void loadPreRequestIntoUI(AstRequest preRequest) {
    isUiLoading = true;

    var configParams = preRequest.getConfigParams();

    this.requestId = configParams.getRequestId();
    this.userId = configParams.getUserId();
    this.requestType = configParams.getRequestType();
    this.active = configParams.isActive();
    this.runParams = preRequest.getRunParams();

    nameText.setValue(configParams.getRequestName());

    urlText.setValue(configParams.getUri());
    methodCombo.setValue(configParams.getMethod());

    headerSection.clearEntries();
    headerSection.addEntries(configParams.getHeaders());
    headerSection.addEntry("", defaultRequestHeader());

    uriVariablesSection.clearEntries();
    uriVariablesSection.addEntries(configParams.getUriVariables());
    uriVariablesSection.addEntry("", defaultRequestUriVariable());

    queryParamsSection.clearEntries();
    queryParamsSection.addEntries(configParams.getQueryParams());
    queryParamsSection.addEntry("", defaultRequestQueryParam());

    requestBodyText.setValue(configParams.getRequestBody() == null
        ? "" : configParams.getRequestBody());

    isUiLoading = false;
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
    var preRequest = new AstRequest(configParams, runParams);

    try {
      astRequestService.update(preRequest);
    } catch (Exception ex) {
      ErrorNotification.show("Error saving parameters");
      throw ex;
    }

    preRequestUpdated(preRequest);
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

}
