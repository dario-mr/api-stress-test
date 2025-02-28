package com.dario.ast.event;

import com.dario.ast.core.domain.RunParams;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class ApplyRunParamsEvent extends ComponentEvent<Component> {

    private final RunParams runParams;

    public ApplyRunParamsEvent(RunParams runParams) {
        super(new UI(), false);
        this.runParams = runParams;
    }
}
