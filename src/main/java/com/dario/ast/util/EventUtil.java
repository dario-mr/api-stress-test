package com.dario.ast.util;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.event.*;
import com.vaadin.flow.component.UI;
import lombok.experimental.UtilityClass;

import static com.vaadin.flow.component.ComponentUtil.fireEvent;

@UtilityClass
public class EventUtil {

    public static void requestConfigParams() {
        fireEvent(UI.getCurrent(), new ConfigParamsRequestEvent());
    }

    public static void requestRunParams() {
        fireEvent(UI.getCurrent(), new RunParamsRequestEvent());
    }

    public static void returnConfigParamsResponse(ConfigParams configParams) {
        fireEvent(UI.getCurrent(), new ConfigParamsResponseEvent(configParams));
    }

    public static void returnRunParamsResponse(RunParams runParams) {
        fireEvent(UI.getCurrent(), new RunParamsResponseEvent(runParams));
    }

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
