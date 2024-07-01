package com.dario.ast.view.component;

import com.dario.ast.core.domain.StressTestParams;
import com.dario.ast.core.service.ParamService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;

import java.util.function.Function;

import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS;

public class SaveButton extends Button {

    public SaveButton(ParamService paramService, Function<Void, StressTestParams> getParamsFunction) {
        setIcon(new SaveIcon());

        addClickListener(event -> {
            try {
                paramService.saveParams(getParamsFunction.apply(null));
                Notification.show("Parameters saved", 2_000, TOP_CENTER).addThemeVariants(LUMO_SUCCESS);
            } catch (JsonProcessingException e) {
                Notification.show("Error saving parameters", 2_000, TOP_CENTER).addThemeVariants(LUMO_ERROR);
                throw new RuntimeException(e);
            }
        });
    }

    private static class SaveIcon extends Icon {

        private SaveIcon() {
            getElement().setAttribute("src", "icons/save-icon.svg");
        }
    }
}
