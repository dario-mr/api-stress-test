package com.dario.ast.core.service.oauth.provider;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthProviderRegistry {

  private final List<OAuthProvider> providers;

  public OAuthProvider get(String registrationId) {
    return providers.stream()
        .filter(p -> p.getRegistrationId().equals(registrationId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No provider for: " + registrationId));
  }

}
