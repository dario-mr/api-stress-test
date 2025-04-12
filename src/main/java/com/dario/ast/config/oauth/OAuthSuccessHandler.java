package com.dario.ast.config.oauth;

import com.dario.ast.repository.OAuthTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

  private final OAuth2AuthorizedClientService authorizedClientService;
  private final OAuthTokenRepository oauthTokenRepository;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    var oauthToken = (OAuth2AuthenticationToken) authentication;
    var userId = oauthToken.getName();
    saveCookie("userId", userId, response);

    var client = authorizedClientService.loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), userId);

    var refreshToken = client.getRefreshToken();
    if (refreshToken != null) {
      oauthTokenRepository.save(userId, refreshToken.getTokenValue(), client.getAccessToken().getExpiresAt());
    }

    response.sendRedirect(request.getContextPath() + "/");
  }

  // todo move to CookieService with encryption, define cookie name as constant/enum
  private void saveCookie(String name, String value, HttpServletResponse response) {
    var cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setAttribute("SameSite", "Lax");
    cookie.setPath("/");
    cookie.setMaxAge((int) Duration.ofDays(400).getSeconds());

    response.addCookie(cookie);
  }

}
