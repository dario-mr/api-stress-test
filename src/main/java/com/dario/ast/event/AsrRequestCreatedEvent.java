package com.dario.ast.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class AsrRequestCreatedEvent extends ComponentEvent<Component> {

    private final Long astRequestId;

    public AsrRequestCreatedEvent(Long astRequestId) {
        super(new UI(), false);
        this.astRequestId = astRequestId;
    }
}
