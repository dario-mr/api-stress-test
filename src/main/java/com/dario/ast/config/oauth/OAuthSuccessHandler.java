package com.dario.ast.config.oauth;

import static com.dario.ast.core.domain.AppCookie.USER_ID;

import com.dario.ast.core.service.oauth.AuthTokenStorageService;
import com.dario.ast.core.service.security.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Custom {@link AuthenticationSuccessHandler} that executes logic upon successful OAuth2 login.
 *
 * <p>This handler performs the following steps after successful authentication:
 * <ul>
 *   <li>Extracts the user ID (OAuth2 "sub" claim) from the authentication token.</li>
 *   <li>Stores the user ID in a secure, encrypted cookie for future session identification.</li>
 *   <li>Persists the refresh token and access token expiry to the database if a refresh token is available.</li>
 *   <li>Redirects the user to the root path of the application (respecting context path).</li>
 * </ul>
 *
 * <p>Designed to support OAuth2 flows where long-term access is required using refresh tokens.
 *
 * <p>This implementation assumes that the user ID is used as the lookup key for storing associated tokens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

  private final OAuth2AuthorizedClientService authorizedClientService;
  private final AuthTokenStorageService authTokenStorageService;
  private final CookieService cookieService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    var oauthToken = (OAuth2AuthenticationToken) authentication;
    var userId = oauthToken.getName();
    var provider = oauthToken.getAuthorizedClientRegistrationId();
    log.debug("Authentication successful for user [{}], provider [{}]", userId, provider);

    cookieService.encryptAndSaveCookie(USER_ID, userId, response);

    var client = authorizedClientService.loadAuthorizedClient(provider, userId);

    var refreshToken = client.getRefreshToken();
    if (refreshToken != null) {
      authTokenStorageService.save(userId, refreshToken.getTokenValue(), provider,
          client.getAccessToken().getExpiresAt());
      log.debug("Stored refresh token in DB for user [{}], access token expires at {}", userId,
          client.getAccessToken().getExpiresAt());
    } else {
      log.warn("No refresh token received for user [{}] (provider [{}])", userId, provider);
    }

    response.sendRedirect(request.getContextPath() + "/");
  }

}
