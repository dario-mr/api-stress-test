package com.dario.ast.config.oauth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
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
@Component
public class OAuthRequestResolver implements OAuth2AuthorizationRequestResolver {

  private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

  public OAuthRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
    this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository, "/oauth2/authorization");
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    var authRequest = defaultResolver.resolve(request);
    if (authRequest == null) {
      return null;
    }

    // asking Google OAuth2 to provide offline access, to generate a refresh_token
    var additionalParams = new HashMap<>(authRequest.getAdditionalParameters());
    additionalParams.put("prompt", "consent");
    additionalParams.put("access_type", "offline");

    return OAuth2AuthorizationRequest.from(authRequest)
        .additionalParameters(additionalParams)
        .build();
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
    return defaultResolver.resolve(request, clientRegistrationId); // fallback to default behavior
  }

}
