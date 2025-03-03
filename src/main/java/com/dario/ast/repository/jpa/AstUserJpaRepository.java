package com.dario.ast.repository.jpa;

import com.dario.ast.repository.jpa.entity.AstUserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AstUserJpaRepository extends JpaRepository<AstUserEntity, Long> {

  Optional<AstUserEntity> findByEmailAndActiveIsTrue(String email);
}
