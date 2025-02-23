package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;

public class AddRunChangeListenerEvent extends ComponentEvent<Component> {

    public AddRunChangeListenerEvent() {
        super(new Button(), false);
    }
}
