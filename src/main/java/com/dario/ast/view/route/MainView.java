package com.dario.ast.view.route;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.ParamService;
import com.dario.ast.core.service.StressService;
import com.dario.ast.view.component.config.ConfigLayout;
import com.dario.ast.view.component.headline.HeadlineLayout;
import com.dario.ast.view.component.notification.ErrorNotification;
import com.dario.ast.view.component.run.RunLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

import static com.dario.ast.util.EventUtil.applyConfigParams;
import static com.dario.ast.util.EventUtil.applyRunParams;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;

@Route
@Slf4j
@RequiredArgsConstructor
@PageTitle("API Stress Test")
public class MainView extends VerticalLayout {

    // TODO save multiple configs to db and show them in a list in the sidebar

    private final static String MAX_WINDOW_WIDTH = "1000px";

    private final StressService stressService;
    private final ParamService paramService;

    @PostConstruct
    public void init() {
        setAlignItems(CENTER);
        setPadding(false);

        var configLayout = new ConfigLayout();
        var configParamsSupplier = (Supplier<ConfigParams>) configLayout::getConfigParams;

        var runLayout = new RunLayout(stressService, configParamsSupplier);
        var runParamsSupplier = (Supplier<RunParams>) runLayout::getRunParams;

        var container = new VerticalLayout(
                new HeadlineLayout(paramService, configParamsSupplier, runParamsSupplier),
                configLayout,
                runLayout
        );
        container.setMaxWidth(MAX_WINDOW_WIDTH);
        add(container);

        getAndApplyParams();
    }

    private void getAndApplyParams() {
        paramService.getParams()
                .thenAccept(params -> {
                    applyConfigParams(params.getConfigParams());
                    applyRunParams(params.getRunParams());
                })
                .exceptionally(ex -> {
                    ErrorNotification.show("Error loading parameters");
                    log.error("Error loading parameters: {}", ex.getMessage());
                    return null;
                });
    }
}