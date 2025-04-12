package com.dario.ast.config.oauth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

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
