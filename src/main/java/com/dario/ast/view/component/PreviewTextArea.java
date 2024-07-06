package com.dario.ast.view.component;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;

import static com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER;
import static com.vaadin.flow.component.notification.NotificationVariant.LUMO_SUCCESS;

public class PreviewTextArea extends TextArea {

    public PreviewTextArea() {
        setLabel("cURL preview");

        setWidthFull();
        setReadOnly(true);
        getStyle()
                .set("padding", "0")
                .set("font-family", "monospace");

        getElement().addEventListener("click", e -> Notification.show("Copied!", 2_000, TOP_CENTER).addThemeVariants(LUMO_SUCCESS));
    }
}
