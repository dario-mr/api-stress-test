package com.dario.ast.view.component.sidebar;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

@UIScope
@SpringComponent
@CssImport(value = "./styles/sidebar.css")
public class Sidebar extends VerticalLayout {

  private final static String MAX_SIDEBAR_WIDTH = "250px";

  @Autowired
  public Sidebar(CreateButton createButton, RequestGrid requestGrid) {
    setWidth("250px");
    setMinWidth("150px");
    setMaxWidth("500px");
    setPadding(false);
    setSpacing(false);
    addClassNames("card-layout", "sidebar-layout");
    getStyle().set("resize", "horizontal").set("overflow", "auto");

    // title + create button
    var title = new H4("Requests");
    title.addClassName("sidebar-title");

    var titleCreateLayout = new HorizontalLayout(title, createButton);
    titleCreateLayout.setWidthFull();
    titleCreateLayout.setFlexGrow(1, title);

    // resize handle
    var resizeHandle = new Div();
    resizeHandle.addClassName("resize-handle");
    resizeHandle.getElement().executeJs(
        "this.addEventListener('mousedown', function(e) {" +
            "   let sidebar = this.parentElement;" +
            "   let startX = e.clientX;" +
            "   let startWidth = sidebar.offsetWidth;" +
            "   function resize(event) {" +
            "       requestAnimationFrame(() => {" +
            "           let newWidth = startWidth + (event.clientX - startX);" +
            "           newWidth = Math.max(150, Math.min(500, newWidth));" +
            "           sidebar.style.width = newWidth + 'px';" +
            "       });" +
            "   }" +
            "   function stopResize() {" +
            "       window.removeEventListener('mousemove', resize);" +
            "       window.removeEventListener('mouseup', stopResize);" +
            "   }" +
            "   window.addEventListener('mousemove', resize);" +
            "   window.addEventListener('mouseup', stopResize);" +
            "});"
    );

    add(titleCreateLayout, requestGrid, resizeHandle);
  }
}
