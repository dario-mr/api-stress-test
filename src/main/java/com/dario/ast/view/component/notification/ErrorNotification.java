package com.dario.ast.view.component.notification;

import com.vaadin.flow.component.notification.Notification;

import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR;

public class ErrorNotification extends Notification {

    private ErrorNotification(String text) {
        setText(text);
        setDuration(3_000);
        setPosition(TOP_CENTER);
        addThemeVariants(LUMO_ERROR);
    }

    public static ErrorNotification show(String text) {
        var notification = new ErrorNotification(text);
        notification.open();

        return notification;
    }
}
