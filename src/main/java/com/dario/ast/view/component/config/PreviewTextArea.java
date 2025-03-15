package com.dario.ast.view.component.config;

import com.dario.ast.view.component.common.notification.SuccessNotification;
import com.vaadin.flow.component.textfield.TextArea;

public class PreviewTextArea extends TextArea {

    public PreviewTextArea() {
        setLabel("cURL preview");

        setWidthFull();
        setReadOnly(true);
        getStyle()
                .set("padding", "0")
                .set("font-family", "monospace")
                .set("font-size", "0.9em");

        getElement().addEventListener("click", e -> SuccessNotification.show("Copied to clipboard!"));
    }
}
