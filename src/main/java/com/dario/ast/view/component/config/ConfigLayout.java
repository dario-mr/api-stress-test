package com.dario.ast.view.component.config;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.event.ApplyConfigParamsEvent;
import com.dario.ast.event.ConfigEntriesUpdatedEvent;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.http.HttpMethod;
import org.vaadin.olli.ClipboardHelper;

import static com.dario.ast.util.CurlPreviewUtil.buildCurlPreview;
import static com.dario.ast.util.MapUtil.removeEmptyEntries;
import static com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap.WRAP;
import static org.springframework.http.HttpMethod.values;

@CssImport(value = "./styles/config-layout.css")
public class ConfigLayout extends VerticalLayout {

    private final TextField urlText = new TextField("API endpoint");
    private final ComboBox<HttpMethod> methodCombo = new ComboBox<>("Method", values());
    private final EntriesSection headerSection = new EntriesSection();
    private final EntriesSection uriVariablesSection = new EntriesSection();
    private final EntriesSection queryParamsSection = new EntriesSection();
    private final TextArea requestBodyText = new TextArea();
    private final ClipboardHelper previewTextClipboard = new ClipboardHelper();
    private final TextArea previewText = new PreviewTextArea();

    public ConfigLayout() {
        setWidthFull();
        addClassNames("card-layout", "config-layout");

        // url + http method
        urlText.setWidth("20em");
        urlText.addValueChangeListener(event -> generateCurlPreview());
        methodCombo.setMaxWidth("7.5em");
        methodCombo.addValueChangeListener(event -> generateCurlPreview());

        var urlMethodLayout = new FlexLayout(urlText, methodCombo);
        urlMethodLayout.setWidthFull();
        urlMethodLayout.setFlexWrap(WRAP);
        urlMethodLayout.setFlexGrow(1, urlText, methodCombo);
        urlMethodLayout.getStyle()
                .set("margin-bottom", "1em")
                .set("gap", "var(--lumo-space-m)");

        // request body
        requestBodyText.setWidthFull();
        requestBodyText.getStyle().set("font-family", "monospace");
        requestBodyText.addValueChangeListener(event -> generateCurlPreview());

        // curl preview
        previewTextClipboard.wrap(previewText);
        previewTextClipboard.getStyle().set("width", "100%");

        // add all components
        add(
                new H3("Configure"),
                urlMethodLayout,
                new ToggleLayout("Headers", headerSection),
                new ToggleLayout("URI Variables", uriVariablesSection),
                new ToggleLayout("Query Parameters", queryParamsSection),
                new ToggleLayout("Request Body", requestBodyText),
                previewTextClipboard
        );
    }

    public ConfigParams getConfigParams() {
        var url = urlText.getValue();
        var method = methodCombo.getValue();
        var headers = removeEmptyEntries(headerSection.getEntries());
        var uriVariables = removeEmptyEntries(uriVariablesSection.getEntries());
        var queryParams = removeEmptyEntries(queryParamsSection.getEntries());
        var requestBody = requestBodyText.getValue();

        return ConfigParams.builder()
                .uri(url)
                .method(method)
                .headers(headers)
                .uriVariables(uriVariables)
                .queryParams(queryParams)
                .requestBody(requestBody)
                .build();
    }

    private void generateCurlPreview() {
        var configParams = getConfigParams();
        var curlPreview = buildCurlPreview(configParams);

        previewText.setValue(curlPreview);
        previewTextClipboard.setContent(curlPreview); // prepare curl preview to be copied to user's clipboard
    }

    private void applyParams(ConfigParams params) {
        urlText.setValue(params.getUri());
        methodCombo.setValue(params.getMethod());

        headerSection.addEntries(params.getHeaders());
        headerSection.addEntry("", "");

        uriVariablesSection.addEntries(params.getUriVariables());
        uriVariablesSection.addEntry("", "");

        queryParamsSection.addEntries(params.getQueryParams());
        queryParamsSection.addEntry("", "");

        requestBodyText.setValue(params.getRequestBody());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Listen for events that should trigger applying the config params in the UI
        ComponentUtil.addListener(attachEvent.getUI(),
                ApplyConfigParamsEvent.class,
                event -> applyParams(event.getConfigParams())
        );

        // Listen for events indicating that config entries (EntriesSection class) were updated
        ComponentUtil.addListener(attachEvent.getUI(),
                ConfigEntriesUpdatedEvent.class,
                event -> generateCurlPreview()
        );
    }
}
