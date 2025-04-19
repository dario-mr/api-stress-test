package com.dario.ast.repository.jpa;

import com.dario.ast.repository.jpa.entity.RequestFolderEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderJpaRepository extends JpaRepository<RequestFolderEntity, Long> {

  List<RequestFolderEntity> findByUserIdOrderByCreatedOn(long userId);

}
