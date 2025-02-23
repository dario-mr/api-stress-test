package com.dario.ast.util;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.event.*;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EventUtil {

    public static void refreshCurlPreview() {
        ComponentUtil.fireEvent(UI.getCurrent(), new RefreshCurlPreviewEvent());
    }

    public static void addConfigChangeListener() {
        ComponentUtil.fireEvent(UI.getCurrent(), new AddConfigChangeListenerEvent());
    }

    public static void addRunChangeListener() {
        ComponentUtil.fireEvent(UI.getCurrent(), new AddRunChangeListenerEvent());
    }

    public static void applyConfigParams(ConfigParams configParams) {
        ComponentUtil.fireEvent(UI.getCurrent(), new ApplyConfigParamsEvent(configParams));
    }

    public static void applyRunParams(RunParams runParams) {
        ComponentUtil.fireEvent(UI.getCurrent(), new ApplyRunParamsEvent(runParams));
    }

    public static void configParamsUpdated(ConfigParams configParams) {
        ComponentUtil.fireEvent(UI.getCurrent(), new ConfigParamsUpdatedEvent(configParams));
    }

    public static void runParamsUpdated(RunParams runParams) {
        ComponentUtil.fireEvent(UI.getCurrent(), new RunParamsUpdatedEvent(runParams));
    }
}
