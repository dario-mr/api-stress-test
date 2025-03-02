package com.dario.ast.view.component.headline;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.service.AstStorageService;
import com.dario.ast.view.component.save.SaveButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.function.Supplier;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN;

public class HeadlineLayout extends HorizontalLayout {

    public HeadlineLayout(AstStorageService astStorageService,
                          Supplier<ConfigParams> configParamsSupplier,
                          Supplier<RunParams> runParamsSupplier) {
        setWidthFull();
        setAlignItems(CENTER);
        setJustifyContentMode(BETWEEN);

        add(
                new Headline(),
                new SaveButton(astStorageService, configParamsSupplier, runParamsSupplier)
        );
    }
}
