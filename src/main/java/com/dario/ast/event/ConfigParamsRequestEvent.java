package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class ConfigParamsRequestEvent extends ComponentEvent<Component> {

    public ConfigParamsRequestEvent() {
        super(new UI(), false);
    }
}
