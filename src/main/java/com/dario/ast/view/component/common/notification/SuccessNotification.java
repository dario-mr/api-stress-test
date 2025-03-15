package com.dario.ast.view.component.common.notification;

import com.vaadin.flow.component.notification.Notification;

import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS;

public class SuccessNotification extends Notification {

    private SuccessNotification(String text) {
        setText(text);
        setDuration(3_000);
        setPosition(TOP_CENTER);
        addThemeVariants(LUMO_SUCCESS);
    }

    public static SuccessNotification show(String text) {
        var notification = new SuccessNotification(text);
        notification.open();

        return notification;
    }
}
