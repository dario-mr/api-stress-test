package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;

public class AddConfigChangeListenerEvent extends ComponentEvent<Component> {

    public AddConfigChangeListenerEvent() {
        super(new Button(), false);
    }
}
