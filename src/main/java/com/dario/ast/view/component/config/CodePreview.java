package com.dario.ast.view.component.config;

import com.dario.ast.view.component.common.notification.SuccessNotification;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;

@Tag("div")
@JsModule("./js/prism/prism-loader.js")
public class CodePreview extends Div {

  private String currentCode = "";

  public CodePreview() {
    setWidthFull();
    getStyle().set("cursor", "pointer");

    // Add click listener to copy text
    getElement().addEventListener("click", e -> {
      getElement().executeJs(
          "navigator.clipboard.writeText($0).then(() => {" +
              "   $1.$server.showCopyNotification();" +
              "}).catch(err => console.error('Failed to copy:', err));",
          currentCode,
          getElement()
      );
    });

    getElement().addEventListener("click", e -> SuccessNotification.show("Copied to clipboard!"));
  }

  public void setCode(String code) {
    this.currentCode = code;
    getElement().setProperty("innerHTML", wrapInPreTag(code));
    getElement().executeJs("Prism.highlightAll()");
  }

  private String wrapInPreTag(String code) {
    return "<pre><code class='language-bash'>" + escapeHtml(code) + "</code></pre>";
  }

  private String escapeHtml(String input) {
    return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
