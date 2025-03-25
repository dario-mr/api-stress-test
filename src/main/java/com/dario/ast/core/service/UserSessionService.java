package com.dario.ast.core.service;

import com.dario.ast.core.domain.OAuthUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletRequest;
import java.io.Serializable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
@RequiredArgsConstructor
public class UserSessionService implements Serializable {

  private static final String LOGOUT_SUCCESS_URL = "/";

  public OAuthUser getUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    var principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();

    return new OAuthUser(
        principal.getAttribute("given_name"),
        principal.getAttribute("family_name"),
        principal.getAttribute("email"),
        principal.getAttribute("picture")
    );
  }

  public boolean isLoggedIn() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null;
  }

  public void logout() {
    // dynamically get the context path
    var contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();

    UI.getCurrent().getPage().setLocation(contextPath + LOGOUT_SUCCESS_URL);
    var logoutHandler = new SecurityContextLogoutHandler();
    logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
  }
}
