package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;

public class ConfigEntriesUpdatedEvent extends ComponentEvent<Component> {

    public ConfigEntriesUpdatedEvent() {
        super(new Button(), false);
    }
}
