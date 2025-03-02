package com.dario.ast.event;

import com.dario.ast.core.domain.AstRequest;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class AsrRequestUpdatedEvent extends ComponentEvent<Component> {

    private final AstRequest astRequest;

    public AsrRequestUpdatedEvent(AstRequest astRequest) {
        super(new UI(), false);
        this.astRequest = astRequest;
    }
}
