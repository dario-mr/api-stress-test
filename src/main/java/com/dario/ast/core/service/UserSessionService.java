package com.dario.ast.core.service;

import com.dario.ast.core.domain.GoogleUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;

@Service
@SessionScope
@RequiredArgsConstructor
public class UserSessionService implements Serializable {

    private static final String LOGOUT_SUCCESS_URL = "/";

    public GoogleUser getUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();

        return new GoogleUser(
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
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        var logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
    }
}
