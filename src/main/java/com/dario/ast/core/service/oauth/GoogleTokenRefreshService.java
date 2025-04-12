package com.dario.ast.core.service.oauth;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class GoogleTokenRefreshService {

  private final RestTemplate restTemplate;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String clientSecret;

  public OAuth2AccessTokenResponse getRefreshedAccessToken(String refreshToken) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    var body = new LinkedMultiValueMap<String, String>();
    body.add("grant_type", "refresh_token");
    body.add("refresh_token", refreshToken);
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);

    var request = new HttpEntity<MultiValueMap<String, String>>(body, headers);

    var response = restTemplate.postForEntity(
        "https://oauth2.googleapis.com/token",
        request,
        Map.class
    );

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException("Failed to refresh access token: " + response.getBody());
    }

    var responseBody = response.getBody();
    String accessToken = (String) responseBody.get("access_token");
    String newRefreshToken = (String) responseBody.get("refresh_token");
    int expiresIn = (Integer) responseBody.get("expires_in");

    return OAuth2AccessTokenResponse.withToken(accessToken)
        .tokenType(BEARER)
        .expiresIn(expiresIn)
        .refreshToken(newRefreshToken != null ? newRefreshToken : refreshToken)
        .build();
  }

  public Map<String, Object> fetchUserInfo(String accessToken) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    var request = new HttpEntity<>(headers);

    var response = restTemplate.exchange(
        "https://www.googleapis.com/oauth2/v3/userinfo",
        GET,
        request,
        Map.class
    );

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException("Failed to fetch user info");
    }

    return response.getBody();
  }

}
