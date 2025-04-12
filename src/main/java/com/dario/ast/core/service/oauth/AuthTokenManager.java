package com.dario.ast.core.service.oauth;

import static com.dario.ast.core.domain.AppCookie.USER_ID;
import static java.time.temporal.ChronoUnit.MINUTES;

import com.dario.ast.core.service.security.CookieService;
import com.dario.ast.repository.OAuthTokenRepository;
import com.dario.ast.repository.jpa.entity.OAuthTokenEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Slf4j
@Component
@SessionScope
@RequiredArgsConstructor
public class AuthTokenManager {
  // TODO refactor?

  private static final int REFRESH_TOKEN_EXPIRATION_IN_MINUTES = 5;

  private final GoogleTokenRefreshService tokenRefreshService;
  private final OAuth2AuthorizedClientService authorizedClientService;
  private final OAuthTokenRepository oauthTokenRepository;
  private final ClientRegistrationRepository clientRegistrationRepository;
  private final CookieService cookieService;

  private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

  public void refreshTokenIfExpired(HttpServletRequest request, HttpServletResponse response) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      refreshByDb(request, response);
    } else {
      refreshBySecurityContext(auth);
    }
  }

  private void refreshByDb(HttpServletRequest request, HttpServletResponse response) {
    var userId = cookieService.getAndDecryptCookie(USER_ID, request);
    if (userId == null) {
      log.info("No userId cookie found, skipping auth token refresh");
      return;
    }

    var refreshToken = oauthTokenRepository.findByUserId(userId)
        .map(OAuthTokenEntity::getRefreshToken)
        .orElse(null);

    if (refreshToken == null) {
      log.info("Empty auth token in DB for user {}", userId);
      return;
    }

    var clientRegistration = clientRegistrationRepository.findByRegistrationId("google");
    var refreshedToken = tokenRefreshService.getRefreshedAccessToken(refreshToken);
    oauthTokenRepository.save(
        userId,
        refreshedToken.getRefreshToken().getTokenValue(),
        refreshedToken.getAccessToken().getExpiresAt());

    var userInfo = tokenRefreshService.fetchUserInfo(refreshedToken.getAccessToken().getTokenValue());

    var oAuth2User = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        userInfo,
        "sub"
    );
    var newAuthToken = new OAuth2AuthenticationToken(
        oAuth2User,
        oAuth2User.getAuthorities(),
        "google"
    );
    SecurityContextHolder.getContext().setAuthentication(newAuthToken);
    securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

    var newClient = new OAuth2AuthorizedClient(
        clientRegistration,
        userId,
        refreshedToken.getAccessToken(),
        refreshedToken.getRefreshToken()
    );

    authorizedClientService.saveAuthorizedClient(newClient, newAuthToken);
    log.info("Refreshed token via DB");
  }

  private void refreshBySecurityContext(Authentication auth) {
    if (!(auth instanceof OAuth2AuthenticationToken authToken)) {
      log.warn("Authentication token is not an OAuth2AuthenticationToken, cannot refresh it");
      return;
    }

    var userId = authToken.getName();
    var client = authorizedClientService.loadAuthorizedClient(authToken.getAuthorizedClientRegistrationId(), userId);
    if (client == null) {
      return;
    }

    var accessToken = client.getAccessToken();
    if (accessToken == null || accessToken.getExpiresAt() == null
        || isAccessTokenStillValid(accessToken.getExpiresAt())) {
      return;
    }

    var authTokenOptional = oauthTokenRepository.findByUserId(userId);
    if (authTokenOptional.isEmpty()) {
      log.info("Empty auth token in DB for user {}", userId);
      return;
    }

    var refreshTokenValue = authTokenOptional.get().getRefreshToken();
    var refreshedTokenResponse = tokenRefreshService.getRefreshedAccessToken(refreshTokenValue);

    oauthTokenRepository.save(
        userId,
        refreshedTokenResponse.getRefreshToken().getTokenValue(),
        refreshedTokenResponse.getAccessToken().getExpiresAt());

    OAuth2AuthorizedClient updatedClient = new OAuth2AuthorizedClient(
        client.getClientRegistration(),
        authToken.getName(),
        refreshedTokenResponse.getAccessToken(),
        refreshedTokenResponse.getRefreshToken()
    );

    authorizedClientService.saveAuthorizedClient(updatedClient, authToken);
    log.info("Refreshed token via security context");
  }

  private static boolean isAccessTokenStillValid(Instant accessTokenExpiresAt) {
    return accessTokenExpiresAt.isAfter(Instant.now().plus(REFRESH_TOKEN_EXPIRATION_IN_MINUTES, MINUTES));
  }

}
