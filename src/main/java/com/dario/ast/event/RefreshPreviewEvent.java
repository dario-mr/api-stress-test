package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;

public class RefreshPreviewEvent extends ComponentEvent<Component> {

    public RefreshPreviewEvent() {
        super(new Button(), false);
    }
}
