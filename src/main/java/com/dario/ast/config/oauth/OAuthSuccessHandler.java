package com.dario.ast.config.oauth;

import static com.dario.ast.core.domain.AppCookie.USER_ID;

import com.dario.ast.core.service.security.CookieService;
import com.dario.ast.repository.OAuthTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
  private final CookieService cookieService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    var oauthToken = (OAuth2AuthenticationToken) authentication;
    var userId = oauthToken.getName();
    cookieService.encryptAndSaveCookie(USER_ID, userId, response);

    var client = authorizedClientService.loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), userId);

    var refreshToken = client.getRefreshToken();
    if (refreshToken != null) {
      oauthTokenRepository.save(userId, refreshToken.getTokenValue(), client.getAccessToken().getExpiresAt());
    }

    response.sendRedirect(request.getContextPath() + "/");
  }

}
