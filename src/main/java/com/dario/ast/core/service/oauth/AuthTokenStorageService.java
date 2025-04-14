package com.dario.ast.core.service.oauth;

import com.dario.ast.core.domain.OAuthToken;
import com.dario.ast.core.service.security.EncryptionService;
import com.dario.ast.repository.AuthTokenRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenStorageService {

  private final AuthTokenRepository authTokenRepository;
  private final EncryptionService encryptionService;

  public void save(String userId, String refreshToken, String provider, Instant expiresAt) {
    var encryptedRefreshToken = encryptionService.encrypt(refreshToken);
    authTokenRepository.save(userId, encryptedRefreshToken, provider, expiresAt);
  }

  public Optional<OAuthToken> findByUserId(String userId) {
    return authTokenRepository.findByUserId(userId)
        .map(oauthToken -> new OAuthToken(
            oauthToken.userId(),
            encryptionService.decrypt(oauthToken.refreshToken()),
            oauthToken.expiresAt(),
            oauthToken.lastUpdated(),
            oauthToken.provider()
        ));
  }

}
