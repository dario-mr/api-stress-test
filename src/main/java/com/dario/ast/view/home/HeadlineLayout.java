package com.dario.ast.view.home;

import com.dario.ast.core.service.ParamService;
import com.dario.ast.view.save.SaveButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN;

public class HeadlineLayout extends HorizontalLayout {

    public HeadlineLayout(ParamService paramService) {
        setWidthFull();
        setAlignItems(CENTER);
        setJustifyContentMode(BETWEEN);

        add(
                new Headline(),
                new SaveButton(paramService)
        );
    }
}
