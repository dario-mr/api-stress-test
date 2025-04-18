package com.dario.ast.repository.jpa;

import com.dario.ast.repository.jpa.entity.RequestEntity;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestJpaRepository extends JpaRepository<RequestEntity, Long> {

  @Query("""
        SELECT r FROM RequestEntity r
        LEFT JOIN FETCH r.folder
        WHERE r.userId = :userId AND r.requestType = :requestType AND r.active = :active
        ORDER BY r.createdOn
      """)
  List<RequestEntity> findByUserIdAndRequestTypeAndActiveOrderByCreatedOn(
      @Param("userId") long userId,
      @Param("requestType") String requestType,
      @Param("active") boolean active
  );

  List<RequestEntity> findByUserIdAndRequestTypeOrderByCreatedOn(long userId, String requestType);

  @Transactional
  void deleteRequestEntitiesByFolderId(long folderId);

}
