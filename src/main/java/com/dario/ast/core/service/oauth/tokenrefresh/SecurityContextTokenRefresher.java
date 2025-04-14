package com.dario.ast.core.service.oauth.tokenrefresh;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;

import com.dario.ast.core.service.oauth.AuthTokenStorageService;
import com.dario.ast.core.service.oauth.provider.OAuthProviderRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityContextTokenRefresher implements TokenRefresher {

  private static final int REFRESH_TOKEN_EXPIRATION_IN_MINUTES = 5;

  private final OAuth2AuthorizedClientService authorizedClientService;
  private final AuthTokenStorageService authTokenStorageService;
  private final OAuthProviderRegistry oauthProviderRegistry;

  @Override
  public void refresh(HttpServletRequest request, HttpServletResponse response) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof OAuth2AuthenticationToken authToken)) {
      log.warn("Authentication object is not an OAuth2AuthenticationToken, cannot refresh it");
      return;
    }

    var userId = authToken.getName();
    var client = authorizedClientService.loadAuthorizedClient(authToken.getAuthorizedClientRegistrationId(), userId);
    if (client == null || client.getAccessToken() == null || client.getAccessToken().getExpiresAt() == null
        || isAccessTokenStillValid(client.getAccessToken().getExpiresAt())) {
      return;
    }

    var storedAuthTokenOptional = authTokenStorageService.findByUserId(userId);
    if (storedAuthTokenOptional.isEmpty()) {
      log.info("Empty auth token in DB for user {}", userId);
      return;
    }

    var oAuthToken = storedAuthTokenOptional.get();
    var refreshTokenValue = oAuthToken.refreshToken();

    if (refreshTokenValue == null) {
      log.warn("Refresh token is null for user {}, skipping token refresh", userId);
      return;
    }

    var provider = oAuthToken.provider();
    var oauthProvider = oauthProviderRegistry.get(provider);
    var refreshedAccessToken = oauthProvider.refreshAccessToken(refreshTokenValue);

    authTokenStorageService.save(
        userId,
        refreshedAccessToken.getRefreshToken().getTokenValue(),
        provider,
        refreshedAccessToken.getAccessToken().getExpiresAt());

    var updatedClient = new OAuth2AuthorizedClient(
        client.getClientRegistration(),
        authToken.getName(),
        refreshedAccessToken.getAccessToken(),
        refreshedAccessToken.getRefreshToken());

    authorizedClientService.saveAuthorizedClient(updatedClient, authToken);

    var newPrincipal = oauthProvider.getUserPrincipal(refreshedAccessToken.getAccessToken().getTokenValue());
    var securityContext = SecurityContextHolder.getContext();
    securityContext.setAuthentication(newPrincipal);

    log.debug("Refreshed token via security context for user {} (provider {})", userId, provider);
  }

  private static boolean isAccessTokenStillValid(Instant accessTokenExpiresAt) {
    return accessTokenExpiresAt.isAfter(now().plus(REFRESH_TOKEN_EXPIRATION_IN_MINUTES, MINUTES));
  }

}
