package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;

public class StressTestParamsRequestEvent extends ComponentEvent<Component> {

    public StressTestParamsRequestEvent() {
        super(new Button(), false);
    }
}
