package com.dario.ast.repository;

import com.dario.ast.core.domain.User;
import com.dario.ast.repository.jpa.AstUserJpaRepository;
import com.dario.ast.repository.jpa.entity.AstUserEntity;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AstUserRepository {

  private final AstUserJpaRepository jpaRepository;

  public Optional<User> findByEmail(String email) {
    return jpaRepository.findByEmailAndActiveIsTrue(email)
        .map(this::mapToDomain);
  }

  public User create(String email) {
    var userEntity = AstUserEntity.builder()
        .id(null)
        .email(email)
        .createdOn(Instant.now())
        .active(true)
        .build();

    return mapToDomain(jpaRepository.save(userEntity));
  }

  private User mapToDomain(AstUserEntity userEntity) {
    return new User(
        userEntity.getId(),
        userEntity.getEmail(),
        userEntity.getCreatedOn(),
        userEntity.isActive());
  }
}
