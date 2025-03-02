package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class UpdateRequestNameEvent extends ComponentEvent<Component> {

    private final String requestName;

    public UpdateRequestNameEvent(String requestName) {
        super(new UI(), false);
        this.requestName = requestName;
    }
}
