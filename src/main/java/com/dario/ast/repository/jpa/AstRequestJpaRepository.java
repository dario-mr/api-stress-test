package com.dario.ast.repository.jpa;

import com.dario.ast.repository.jpa.entity.RequestEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AstRequestJpaRepository extends JpaRepository<RequestEntity, Long> {

  List<RequestEntity> findByUserIdAndRequestTypeAndActiveOrderByCreatedOn(
      long userId, String requestType, boolean active);
}
