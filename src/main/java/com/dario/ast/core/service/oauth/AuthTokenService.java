package com.dario.ast.core.service.oauth;

import com.dario.ast.core.domain.OAuthToken;
import com.dario.ast.core.service.security.EncryptionService;
import com.dario.ast.repository.OAuthTokenRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

  private final OAuthTokenRepository oAuthTokenRepository;
  private final EncryptionService encryptionService;

  public void save(String userId, String refreshToken, Instant expiresAt) {
    var encryptedRefreshToken = encryptionService.encrypt(refreshToken);
    oAuthTokenRepository.save(userId, encryptedRefreshToken, expiresAt);
  }

  public Optional<OAuthToken> findByUserId(String userId) {
    return oAuthTokenRepository.findByUserId(userId)
        .map(oauthToken -> new OAuthToken(
            oauthToken.userId(),
            encryptionService.decrypt(oauthToken.refreshToken()),
            oauthToken.expiresAt(),
            oauthToken.lastUpdated()
        ));
  }

}
