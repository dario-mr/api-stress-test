package com.dario.ast.core.service;

import static com.dario.ast.core.domain.AppCookie.USER_ID;

import com.dario.ast.core.domain.OAuthUser;
import com.dario.ast.core.service.security.CookieService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletRequest;
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

  private final CookieService cookieService;

  public OAuthUser getUser() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof OAuth2AuthenticationToken authToken)) {
      throw new IllegalStateException("OAuth2 authentication not found in the security context");
    }

    var principal = authToken.getPrincipal();

    // TODO attributes might differ for other auth providers: abstract this
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
    cookieService.deleteCookie(USER_ID, response);

    // perform Spring Security logout
    var request = VaadinServletRequest.getCurrent().getHttpServletRequest();
    new SecurityContextLogoutHandler().logout(request, null, null);
  }

}
