package com.dario.ast.view.component.config;

import com.dario.ast.view.component.common.notification.SuccessNotification;
import com.vaadin.flow.component.textfield.TextArea;

public class PreviewTextArea extends TextArea {

  public PreviewTextArea() {
    setWidthFull();
    setReadOnly(true);
    // for some reason, css selector "vaadin-text-area" does not apply here, because "previewText" is wrapped in "ClipboardHelper"
    getStyle()
        .set("padding", "0")
        .set("font-family", "monospace")
        .set("font-size", "var(--lumo-font-size-s)");

    getElement().addEventListener("click", e -> SuccessNotification.show("Copied to clipboard!"));
  }

}
