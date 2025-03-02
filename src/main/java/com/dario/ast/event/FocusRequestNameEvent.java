package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class FocusRequestNameEvent extends ComponentEvent<Component> {

    public FocusRequestNameEvent() {
        super(new UI(), false);
    }
}
