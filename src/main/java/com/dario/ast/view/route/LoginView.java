package com.dario.ast.view.route;

import com.dario.ast.view.component.headline.Headline;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Value;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
@CssImport(value = "./styles/login-layout.css")
public class LoginView extends VerticalLayout {

  public LoginView(@Value("${oath.url}") String oauthUrl) {
    setAlignItems(Alignment.CENTER);

    var googleIcon = new Image("/images/google.png", "Google Logo");
    googleIcon.setHeight("35px");
    googleIcon.setWidth("35px");

    var buttonContent = new HorizontalLayout(googleIcon, new Span("Sign in with Google"));
    buttonContent.setAlignItems(Alignment.CENTER);
    buttonContent.setSpacing(true);

    var loginButton = new Button(buttonContent);
    loginButton.addClassNames("google-login-button");
    loginButton.addClickListener(e -> getUI().ifPresent(ui ->
        ui.getPage().executeJs("window.location.href = $0;", oauthUrl)));

    add(
        new Headline(),
        loginButton
    );
  }
}