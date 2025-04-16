package com.dario.ast.repository;

import com.dario.ast.core.domain.OAuthToken;
import com.dario.ast.repository.jpa.AuthTokenJpaRepository;
import com.dario.ast.repository.jpa.entity.OAuthTokenEntity;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthTokenRepository {

  private final AuthTokenJpaRepository jpaRepository;

  public void save(String userId, String refreshToken, String provider, Instant expiresAt) {
    var entity = OAuthTokenEntity.builder()
        .userId(userId)
        .refreshToken(refreshToken)
        .provider(provider)
        .expiresAt(expiresAt)
        .lastUpdated(Instant.now())
        .build();

    jpaRepository.save(entity);
  }

  public Optional<OAuthToken> findByUserId(String userId) {
    return jpaRepository.findById(userId)
        .map(this::mapToDomain);
  }

  private OAuthToken mapToDomain(OAuthTokenEntity entity) {
    return new OAuthToken(
        entity.getUserId(),
        entity.getRefreshToken(),
        entity.getExpiresAt(),
        entity.getLastUpdated(),
        entity.getProvider()
    );
  }

}
