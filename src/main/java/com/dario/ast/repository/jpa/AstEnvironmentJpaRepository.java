package com.dario.ast.repository.jpa;

import com.dario.ast.repository.jpa.entity.AstEnvironmentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AstEnvironmentJpaRepository extends JpaRepository<AstEnvironmentEntity, Long> {

  List<AstEnvironmentEntity> findByUserIdOrderByCreatedOn(long userId);
}
