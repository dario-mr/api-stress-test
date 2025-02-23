package com.dario.ast.view.component;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.domain.StressTestParams;
import com.dario.ast.core.service.ParamService;
import com.dario.ast.event.ConfigParamsUpdatedEvent;
import com.dario.ast.event.RunParamsUpdatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;

import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS;

public class SaveButton extends Button {

    private RunParams currentRunParams;
    private ConfigParams currentConfigParams;

    public SaveButton(ParamService paramService) {
        setIcon(new SaveIcon());

        addClickListener(event -> {
            try {
                paramService.saveParams(new StressTestParams(currentConfigParams, currentRunParams));
                Notification.show("Parameters saved", 2_000, TOP_CENTER).addThemeVariants(LUMO_SUCCESS);
            } catch (JsonProcessingException e) {
                Notification.show("Error saving parameters", 2_000, TOP_CENTER).addThemeVariants(LUMO_ERROR);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Listen for "config params updated" events
        ComponentUtil.addListener(
                attachEvent.getUI(),
                ConfigParamsUpdatedEvent.class,
                event -> this.currentConfigParams = event.getConfigParams()
        );

        // Listen for "run params updated" events
        ComponentUtil.addListener(
                attachEvent.getUI(),
                RunParamsUpdatedEvent.class,
                event -> this.currentRunParams = event.getRunParams()
        );
    }

    private static class SaveIcon extends Icon {

        private SaveIcon() {
            getElement().setAttribute("src", "icons/save-icon.svg");
        }
    }
}
