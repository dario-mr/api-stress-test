package com.dario.ast.core.service.oauth.tokenrefresh;

import static com.dario.ast.core.domain.AppCookie.USER_ID;

import com.dario.ast.core.service.oauth.AuthTokenStorageService;
import com.dario.ast.core.service.oauth.provider.OAuthProviderRegistry;
import com.dario.ast.core.service.security.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseTokenRefresher implements TokenRefresher {

  private final AuthTokenStorageService authTokenStorageService;
  private final CookieService cookieService;
  private final ClientRegistrationRepository clientRegistrationRepository;
  private final OAuthProviderRegistry oauthProviderRegistry;
  private final OAuth2AuthorizedClientService authorizedClientService;
  private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

  @Override
  public void refresh(HttpServletRequest request, HttpServletResponse response) {
    var userId = cookieService.getAndDecryptCookie(USER_ID, request);
    if (userId == null) {
      return;
    }

    var oauthTokenOptional = authTokenStorageService.findByUserId(userId);
    if (oauthTokenOptional.isEmpty()) {
      log.debug("No auth token found for user {}, skipping refresh", userId);
      return;
    }

    var oauthToken = oauthTokenOptional.get();
    var provider = oauthToken.provider();

    var clientRegistration = clientRegistrationRepository.findByRegistrationId(provider);
    var oauthProvider = oauthProviderRegistry.get(provider);

    var refreshedAccessToken = oauthProvider.refreshAccessToken(oauthToken.refreshToken());

    authTokenStorageService.save(
        userId,
        refreshedAccessToken.getRefreshToken().getTokenValue(),
        provider,
        refreshedAccessToken.getAccessToken().getExpiresAt());

    var accessToken = refreshedAccessToken.getAccessToken().getTokenValue();
    var principal = oauthProvider.getUserPrincipal(accessToken);

    var securityContext = SecurityContextHolder.getContext();
    securityContext.setAuthentication(principal);
    securityContextRepository.saveContext(securityContext, request, response);

    var newClient = new OAuth2AuthorizedClient(
        clientRegistration,
        userId,
        refreshedAccessToken.getAccessToken(),
        refreshedAccessToken.getRefreshToken());

    authorizedClientService.saveAuthorizedClient(newClient, principal);
    log.debug("Refreshed token via DB for user {} (provider {})", userId, provider);
  }
}
