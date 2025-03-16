package com.dario.ast.repository;

import static java.util.stream.Collectors.toMap;

import com.dario.ast.core.domain.EnvVariable;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.repository.jpa.AstEnvironmentJpaRepository;
import com.dario.ast.repository.jpa.entity.AstEnvVariableEntity;
import com.dario.ast.repository.jpa.entity.AstEnvironmentEntity;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AstEnvironmentRepository {

  private final AstEnvironmentJpaRepository jpaRepository;

  public List<Environment> findByUserId(long userId) {
    return jpaRepository.findByUserIdOrderByCreatedOn(userId).stream()
        .map(this::mapToDomain)
        .toList();
  }

  public Optional<Environment> findById(Long id) {
    return jpaRepository.findById(id)
        .map(this::mapToDomain);
  }

  public Long create(Environment environment) {
    var entity = mapToEntity(environment);
    entity.setId(null);
    entity.setCreatedOn(Instant.now());

    return jpaRepository.save(entity).getId();
  }

  public void update(Environment environment) {
    var oldEntity = jpaRepository.findById(environment.getId()).orElseThrow();
    var newEntity = mapToEntity(environment);
    newEntity.setCreatedOn(oldEntity.getCreatedOn());

    jpaRepository.save(newEntity);
  }

  public void delete(long id) {
    jpaRepository.deleteById(id);
  }

  private Environment mapToDomain(AstEnvironmentEntity entity) {
    return new Environment(
        entity.getId(),
        entity.getName(),
        entity.getUserId(),
        entity.getVariables()
            .entrySet().stream()
            .collect(toMap(
                Entry::getKey,
                entry -> new EnvVariable(
                    entry.getValue().getValue(),
                    entry.getValue().getCreatedOn()),
                (e1, e2) -> e1,
                LinkedHashMap::new))
    );
  }

  private AstEnvironmentEntity mapToEntity(Environment environment) {
    return AstEnvironmentEntity.builder()
        .id(environment.getId())
        .name(environment.getName())
        .userId(environment.getUserId())
        .variables(environment.getVariables()
            .entrySet().stream()
            .collect(toMap(
                Entry::getKey,
                entry -> new AstEnvVariableEntity(
                    entry.getValue().getValue(),
                    entry.getValue().getCreatedOn()
                )))
        )
        .build();
  }
}
