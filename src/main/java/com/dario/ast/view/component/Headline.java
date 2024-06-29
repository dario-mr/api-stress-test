package com.dario.ast.view.component;

import com.vaadin.flow.component.html.H2;

public class Headline extends H2 {

    public Headline() {
        setText("API Stress Test");
        addClassName("headline");
    }
}
