package com.dario.ast.core.service.oauth.provider.google;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

import com.dario.ast.core.exception.OAuthTokenRefreshException;
import com.dario.ast.core.service.oauth.provider.OAuthProvider;
import com.dario.ast.core.service.oauth.provider.google.dto.GoogleTokenResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GoogleOAuthProvider implements OAuthProvider {

  private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
  private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String clientSecret;

  private final RestTemplate restTemplate;

  @Override
  public String getRegistrationId() {
    return "google";
  }

  @Override
  public OAuth2AccessTokenResponse refreshAccessToken(String refreshToken) {
    var request = buildTokenRequest(refreshToken);
    var response = restTemplate.postForEntity(TOKEN_URL, request, GoogleTokenResponse.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new OAuthTokenRefreshException("Failed to refresh access token: " + response.getBody());
    }

    return toOAuth2AccessTokenResponse(response.getBody(), refreshToken);
  }

  @Override
  public Map<String, Object> fetchUserInfo(String accessToken) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    var request = new HttpEntity<>(headers);

    var response = restTemplate.exchange(USER_INFO_URL, GET, request,
        new ParameterizedTypeReference<Map<String, Object>>() {
        }
    );

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new OAuthTokenRefreshException("Failed to fetch user info from Google");
    }

    return response.getBody();
  }

  @Override
  public OAuth2AuthenticationToken getUserPrincipal(String accessToken) {
    var userInfo = fetchUserInfo(accessToken);
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    // Google uses "sub" as the unique ID claim
    var user = new DefaultOAuth2User(authorities, userInfo, "sub");

    return new OAuth2AuthenticationToken(user, authorities, getRegistrationId());
  }

  @Override
  public OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest request) {
    // parameters required to make google generate a refresh token
    var additionalParams = new HashMap<>(request.getAdditionalParameters());
    additionalParams.put("prompt", "consent");
    additionalParams.put("access_type", "offline");

    return OAuth2AuthorizationRequest.from(request)
        .additionalParameters(additionalParams)
        .build();
  }

  private HttpEntity<MultiValueMap<String, String>> buildTokenRequest(String refreshToken) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    var body = new LinkedMultiValueMap<String, String>();
    body.add("grant_type", "refresh_token");
    body.add("refresh_token", refreshToken);
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);

    return new HttpEntity<>(body, headers);
  }

  private OAuth2AccessTokenResponse toOAuth2AccessTokenResponse(GoogleTokenResponse response, String oldRefreshToken) {
    var accessToken = response.accessToken();
    var expiresIn = response.expiresIn();
    var refreshToken = response.refreshToken() != null ? response.refreshToken() : oldRefreshToken;
    Set<String> scopes = response.scope() != null
        ? Set.of(response.scope().split(" "))
        : Set.of();

    return OAuth2AccessTokenResponse.withToken(accessToken)
        .tokenType(BEARER)
        .expiresIn(expiresIn)
        .refreshToken(refreshToken)
        .scopes(scopes)
        .build();
  }

}
