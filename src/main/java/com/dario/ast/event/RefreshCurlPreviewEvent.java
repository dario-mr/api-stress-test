package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;

public class RefreshCurlPreviewEvent extends ComponentEvent<Component> {

    public RefreshCurlPreviewEvent() {
        super(new Button(), false);
    }
}
