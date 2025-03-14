package com.dario.ast.repository.jpa;

import com.dario.ast.repository.jpa.entity.AstRequestEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AstRequestJpaRepository extends JpaRepository<AstRequestEntity, Long> {

  List<AstRequestEntity> findByUserIdOrderByCreatedOn(long userId);
}
