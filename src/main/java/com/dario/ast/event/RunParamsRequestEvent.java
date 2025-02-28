package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class RunParamsRequestEvent extends ComponentEvent<Component> {

    public RunParamsRequestEvent() {
        super(new UI(), false);
    }
}
