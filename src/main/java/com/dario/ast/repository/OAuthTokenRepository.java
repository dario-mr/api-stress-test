package com.dario.ast.repository;

import com.dario.ast.repository.jpa.OAuthTokenJpaRepository;
import com.dario.ast.repository.jpa.entity.OAuthTokenEntity;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OAuthTokenRepository {

  private final OAuthTokenJpaRepository jpaRepository;

  public void save(String userId, String refreshToken, Instant expiresAt) {
    var entity = OAuthTokenEntity.builder()
        .userId(userId)
        .refreshToken(refreshToken)
        .expiresAt(expiresAt)
        .lastUpdated(Instant.now())
        .build();

    jpaRepository.save(entity);
  }

  public Optional<OAuthTokenEntity> findByUserId(String userId) {
    return jpaRepository.findById(userId);
  }

}
