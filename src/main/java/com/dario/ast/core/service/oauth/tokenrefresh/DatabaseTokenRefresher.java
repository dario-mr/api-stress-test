package com.dario.ast.core.service.oauth.tokenrefresh;

import static com.dario.ast.core.domain.AppCookie.USER_ID;

import com.dario.ast.core.domain.OAuthToken;
import com.dario.ast.core.service.oauth.AuthTokenStorageService;
import com.dario.ast.core.service.security.CookieService;
import com.dario.ast.proxy.google.GoogleTokenRefreshService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseTokenRefresher implements TokenRefreshStrategy {

  private final AuthTokenStorageService authTokenStorageService;
  private final CookieService cookieService;
  private final ClientRegistrationRepository clientRegistrationRepository;
  private final GoogleTokenRefreshService googleTokenRefreshService;
  private final OAuth2AuthorizedClientService authorizedClientService;
  private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

  @Override
  public void refresh(HttpServletRequest request, HttpServletResponse response) {
    var userId = cookieService.getAndDecryptCookie(USER_ID, request);
    if (userId == null) {
      log.debug("No userId cookie found, skipping auth token refresh");
      return;
    }

    var storedRefreshToken = authTokenStorageService.findByUserId(userId)
        .map(OAuthToken::refreshToken)
        .orElse(null);

    if (storedRefreshToken == null) {
      log.debug("Empty auth token in DB for user {}, skipping auth token refresh", userId);
      return;
    }

    var clientRegistration = clientRegistrationRepository.findByRegistrationId("google");
    var refreshedAccessToken = googleTokenRefreshService.getRefreshedAccessToken(storedRefreshToken);

    authTokenStorageService.save(userId,
        refreshedAccessToken.getRefreshToken().getTokenValue(),
        refreshedAccessToken.getAccessToken().getExpiresAt());

    var userInfo = googleTokenRefreshService.fetchUserInfo(refreshedAccessToken.getAccessToken().getTokenValue());
    var oAuth2User = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_USER")), userInfo, "sub");
    var newAuthToken = new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), "google");

    SecurityContextHolder.getContext().setAuthentication(newAuthToken);
    securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

    var newClient = new OAuth2AuthorizedClient(clientRegistration, userId,
        refreshedAccessToken.getAccessToken(),
        refreshedAccessToken.getRefreshToken());

    authorizedClientService.saveAuthorizedClient(newClient, newAuthToken);
    log.debug("Refreshed token via DB");
  }
}
