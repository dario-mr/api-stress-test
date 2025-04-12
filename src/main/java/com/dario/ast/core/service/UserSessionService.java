package com.dario.ast.core.service;

import com.dario.ast.core.domain.OAuthUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
@RequiredArgsConstructor
public class UserSessionService implements Serializable {

  private static final String LOGOUT_SUCCESS_URL = "/";

  public OAuthUser getUser() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof OAuth2AuthenticationToken authToken)) {
      throw new IllegalStateException("OAuth2 authentication not found in the session");
    }

    var principal = authToken.getPrincipal();

    return new OAuthUser(
        principal.getAttribute("given_name"),
        principal.getAttribute("family_name"),
        principal.getAttribute("email"),
        principal.getAttribute("picture")
    );
  }

  public boolean isLoggedIn() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    return auth instanceof OAuth2AuthenticationToken && auth.isAuthenticated();
  }

  public void logout() {
    // dynamically get the context path (required if service is deployed in sub-domain)
    var contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();

    UI.getCurrent().getPage().setLocation(contextPath + LOGOUT_SUCCESS_URL);

    // delete userId cookie
    var response = (HttpServletResponse) VaadinResponse.getCurrent();
    deleteCookie(response, "userId");

    // perform Spring Security logout
    var request = VaadinServletRequest.getCurrent().getHttpServletRequest();
    new SecurityContextLogoutHandler().logout(request, null, null);
  }

  private void deleteCookie(HttpServletResponse response, String cookieName) {
    Cookie cookie = new Cookie(cookieName, null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

}
