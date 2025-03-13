package com.dario.ast.repository;

import com.dario.ast.core.domain.Environment;
import com.dario.ast.repository.jpa.AstEnvironmentJpaRepository;
import com.dario.ast.repository.jpa.entity.AstEnvironmentEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AstEnvironmentRepository {

  private final AstEnvironmentJpaRepository jpaRepository;

  public List<Environment> findByUserEmail(String userEmail) {
    return jpaRepository.findByUser_EmailOrderByCreatedOn(userEmail).stream()
        .map(this::mapToDomain)
        .toList();
  }

  private Environment mapToDomain(AstEnvironmentEntity entity) {
    return new Environment(entity.getName());
  }

}
