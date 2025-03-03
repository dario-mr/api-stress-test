package com.dario.ast.view.route;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.core.service.StressTestService;
import com.dario.ast.view.component.config.ConfigLayout;
import com.dario.ast.view.component.headline.HeadlineLayout;
import com.dario.ast.view.component.run.RunLayout;
import com.dario.ast.view.component.sidebar.Sidebar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@Route
@RequiredArgsConstructor
@PageTitle("API Stress Test")
public class MainView extends VerticalLayout {

    // TODO check if anything can be turned into a record
    // TODO add Google oauth
    // TODO remove save button, save automatically on value change
    // TODO folders...

    private final StressTestService stressTestService;
    private final AstRequestService astRequestService;

    @PostConstruct
    public void init() {
        setSizeFull();

        var configLayout = new ConfigLayout();
        var configParamsSupplier = (Supplier<ConfigParams>) configLayout::getConfigParams;

        var runLayout = new RunLayout(stressTestService, configParamsSupplier);
        var runParamsSupplier = (Supplier<RunParams>) runLayout::getRunParams;

        var requestLayout = new VerticalLayout(configLayout, runLayout);
        requestLayout.setPadding(false);

        var headline = new HeadlineLayout(astRequestService, configParamsSupplier, runParamsSupplier);

        var sidebar = new Sidebar(astRequestService);

        var mainContent = new HorizontalLayout(sidebar, requestLayout);
        mainContent.setSizeFull();

        // add all components
        add(headline, mainContent);
    }
}