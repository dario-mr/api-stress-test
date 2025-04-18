package com.dario.ast.repository.jpa;

import com.dario.ast.repository.jpa.entity.EnvironmentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvironmentJpaRepository extends JpaRepository<EnvironmentEntity, Long> {

  List<EnvironmentEntity> findByUserIdOrderByCreatedOn(long userId);
}
