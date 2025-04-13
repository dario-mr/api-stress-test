package com.dario.ast.proxy.google;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

import com.dario.ast.core.exception.OAuthTokenRefreshException;
import com.dario.ast.proxy.google.dto.GoogleTokenResponse;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GoogleTokenProxy {

  private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
  private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String clientSecret;

  private final RestTemplate restTemplate;

  public OAuth2AccessTokenResponse getRefreshedAccessToken(String refreshToken) {
    var request = buildTokenRequest(refreshToken);
    var response = restTemplate.postForEntity(TOKEN_URL, request, GoogleTokenResponse.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new OAuthTokenRefreshException("Failed to refresh access token: " + response.getBody());
    }

    return toOAuth2AccessTokenResponse(response.getBody(), refreshToken);
  }

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