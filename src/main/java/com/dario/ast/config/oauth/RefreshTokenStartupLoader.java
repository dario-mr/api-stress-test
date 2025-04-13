package com.dario.ast.config.oauth;

import com.dario.ast.core.service.oauth.AuthTokenStorageService;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Load the Google refresh token in the in-memory H2 db at startup, only on dev profile, reading it from the env
 * variable {@code DEV_REFRESH_TOKEN}.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class RefreshTokenStartupLoader {

  @Value("${oauth.google.dev-refresh-token}")
  private String devRefreshToken;

  private final AuthTokenStorageService authTokenStorageService;

  @PostConstruct
  public void loadRefreshTokenIntoH2() {
    authTokenStorageService.save("111235512772934408325", devRefreshToken, Instant.now());
  }

}

