package com.dario.ast.event;

import com.dario.ast.core.domain.RunParams;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import lombok.Getter;

@Getter
public class RunParamsUpdatedEvent extends ComponentEvent<Component> {

    private final RunParams runParams;

    public RunParamsUpdatedEvent(RunParams runParams) {
        super(new Button(), false);
        this.runParams = runParams;
    }
}
