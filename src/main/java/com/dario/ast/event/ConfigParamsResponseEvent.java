package com.dario.ast.event;

import com.dario.ast.core.domain.ConfigParams;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import lombok.Getter;

@Getter
public class ConfigParamsResponseEvent extends ComponentEvent<Component> {

    private final ConfigParams configParams;

    public ConfigParamsResponseEvent(ConfigParams configParams) {
        super(new Button(), false);
        this.configParams = configParams;
    }
}
