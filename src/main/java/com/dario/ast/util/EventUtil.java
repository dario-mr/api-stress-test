package com.dario.ast.util;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.event.*;
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

    public static void asrRequestUpdated(AstRequest astRequest) {
        fireEvent(UI.getCurrent(), new AsrRequestUpdatedEvent(astRequest));
    }

    public static void asrRequestCreated(Long asrRequestId) {
        fireEvent(UI.getCurrent(), new AsrRequestCreatedEvent(asrRequestId));
    }

    public static void focusRequestName() {
        fireEvent(UI.getCurrent(), new FocusRequestNameEvent());
    }
}
