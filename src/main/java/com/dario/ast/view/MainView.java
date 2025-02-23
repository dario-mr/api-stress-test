package com.dario.ast.view;

import com.dario.ast.core.domain.StressTestParams;
import com.dario.ast.core.service.ParamService;
import com.dario.ast.core.service.StressService;
import com.dario.ast.view.component.Headline;
import com.dario.ast.view.component.SaveButton;
import com.dario.ast.view.config.ConfigLayout;
import com.dario.ast.view.run.RunLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.dario.ast.util.EventUtil.*;
import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN;

@Route
@Slf4j
@RequiredArgsConstructor
@PageTitle("API Stress Test")
public class MainView extends VerticalLayout {

    // TODO validation when clicking start
    // TODO results history?
    // TODO persist parameters to db?
    // TODO save configs in a list in the sidebar

    private final static String MAX_WINDOW_WIDTH = "1000px";

    private final StressService stressService;
    private final ParamService paramService;

    @PostConstruct
    public void init() {
        setAlignItems(CENTER);
        setPadding(false);

        // headline
        var headlineLayout = new HorizontalLayout(
                new Headline(),
                new SaveButton(paramService));
        headlineLayout.setWidthFull();
        headlineLayout.setAlignItems(CENTER);
        headlineLayout.setJustifyContentMode(BETWEEN);

        // container with everything
        var container = new VerticalLayout(
                headlineLayout,
                new ConfigLayout(),
                new RunLayout(stressService)
        );
        container.setMaxWidth(MAX_WINDOW_WIDTH);
        add(container);

        getAndApplyParams();
    }

    private void getAndApplyParams() {
        paramService.getParams()
                .thenAccept(this::applyParams)
                .exceptionally(ex -> {
                    Notification.show("Error loading parameters", 3_000, TOP_CENTER).addThemeVariants(LUMO_ERROR);
                    log.error("Error loading parameters: {}", ex.getMessage());
                    return null;
                })
                .thenAccept(unused -> refreshCurlPreview())
                // add the "value change" listeners after having set the values the first time, to avoid setting them multiple times
                .thenAccept(unused -> addConfigChangeListener())
                .thenAccept(unused -> addRunChangeListener());
    }

    private void applyParams(StressTestParams params) {
        applyConfigParams(params.getConfigParams());
        applyRunParams(params.getRunParams());

        configParamsUpdated(params.getConfigParams());
        runParamsUpdated(params.getRunParams());
    }

}