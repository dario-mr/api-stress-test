package com.dario.ast.core.service.oauth.provider;

import java.util.Map;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

public interface OAuthProvider {

  String getRegistrationId();

  OAuth2AccessTokenResponse refreshAccessToken(String refreshToken);

  Map<String, Object> fetchUserInfo(String accessToken);

  OAuth2AuthenticationToken getUserPrincipal(String accessToken);

}
