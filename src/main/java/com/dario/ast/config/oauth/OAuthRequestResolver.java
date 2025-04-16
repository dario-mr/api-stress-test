package com.dario.ast.config.oauth;

import com.dario.ast.core.service.oauth.provider.OAuthProviderRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * Custom implementation of {@link OAuth2AuthorizationRequestResolver} that modifies the default OAuth2 authorization
 * request to explicitly request a refresh token from Google by setting additional parameters.
 *
 * <p>This resolver wraps Spring Security's default resolver and overrides the request
 * to include:
 * <ul>
 *   <li>{@code prompt=consent} — ensures the consent screen is shown every time, which is
 *       required by Google to issue a refresh token.</li>
 *   <li>{@code access_type=offline} — instructs Google to provide a refresh token in
 *       addition to the access token.</li>
 * </ul>
 *
 * <p>This is useful when the application needs long-lived access and intends to refresh
 * tokens without user interaction.
 *
 * <p>Only modifies the request when resolving without an explicit client registration ID;
 * otherwise delegates to the default behavior.
 */
@Slf4j
@Component
public class OAuthRequestResolver implements OAuth2AuthorizationRequestResolver {

  private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
  private final OAuthProviderRegistry oauthProviderRegistry;

  public OAuthRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuthProviderRegistry oauthProviderRegistry
  ) {
    this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository, "/oauth2/authorization");
    this.oauthProviderRegistry = oauthProviderRegistry;
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    var authRequest = defaultResolver.resolve(request);
    return customizeIfNeeded(authRequest);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
    var authRequest = defaultResolver.resolve(request, clientRegistrationId);
    return customizeIfNeeded(authRequest);
  }

  private OAuth2AuthorizationRequest customizeIfNeeded(OAuth2AuthorizationRequest authRequest) {
    if (authRequest == null) {
      return null;
    }

    var registrationId = (String) authRequest.getAttribute("registration_id");
    if (registrationId == null) {
      log.warn("Authorization request missing registration_id attribute");
      return authRequest;
    }

    var oauthProvider = oauthProviderRegistry.getOrNull(registrationId);
    if (oauthProvider != null) {
      log.debug("Applying additional parameters for provider [{}]", registrationId);
      return oauthProvider.customizeAuthorizationRequest(authRequest);
    }

    log.debug("No additional parameters applied for provider [{}]", registrationId);
    return authRequest;
  }

}