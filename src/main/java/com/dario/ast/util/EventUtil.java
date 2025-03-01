package com.dario.ast.util;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.event.ApplyConfigParamsEvent;
import com.dario.ast.event.ApplyRunParamsEvent;
import com.dario.ast.event.ConfigEntriesUpdatedEvent;
import com.vaadin.flow.component.UI;
import lombok.experimental.UtilityClass;

import static com.vaadin.flow.component.ComponentUtil.fireEvent;

@UtilityClass
public class EventUtil {

    public static void applyConfigParams(ConfigParams configParams) {
        fireEvent(UI.getCurrent(), new ApplyConfigParamsEvent(configParams));
    }

    public static void applyRunParams(RunParams runParams) {
        fireEvent(UI.getCurrent(), new ApplyRunParamsEvent(runParams));
    }

    public static void configEntriesUpdated() {
        fireEvent(UI.getCurrent(), new ConfigEntriesUpdatedEvent());
    }
}
