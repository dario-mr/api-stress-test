package com.dario.ast.repository.jpa;

import com.dario.ast.repository.jpa.entity.RequestEntity;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestJpaRepository extends JpaRepository<RequestEntity, Long> {

  List<RequestEntity> findByUserIdAndRequestTypeOrderByCreatedOn(long userId, String requestType);

  @Transactional
  void deleteRequestEntitiesByFolderId(long folderId);

}
