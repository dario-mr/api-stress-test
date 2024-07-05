package com.dario.ast.util;

import com.dario.ast.event.RefreshPreviewEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EventUtil {

    public static void refreshPreview() {
        ComponentUtil.fireEvent(UI.getCurrent(), new RefreshPreviewEvent());
    }
}
