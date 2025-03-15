package com.dario.ast.view.component.common.notification;

import com.vaadin.flow.component.notification.Notification;

import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_WARNING;

public class WarnNotification extends Notification {

    private WarnNotification(String text) {
        setText(text);
        setDuration(3_000);
        setPosition(TOP_CENTER);
        addThemeVariants(LUMO_WARNING);
    }

    public static WarnNotification show(String text) {
        var notification = new WarnNotification(text);
        notification.open();

        return notification;
    }
}
