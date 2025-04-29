package com.dario.ast.repository;

import com.dario.ast.core.domain.Folder;
import com.dario.ast.repository.jpa.FolderJpaRepository;
import com.dario.ast.repository.jpa.entity.RequestFolderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FolderRepository {

  private final FolderJpaRepository jpaRepository;

  public Folder save(Folder folder) {
    var entity = jpaRepository.save(toEntity(folder));
    return toDomain(entity);
  }

  public void delete(long id) {
    jpaRepository.deleteById(id);
  }

  private RequestFolderEntity toEntity(Folder folder) {
    return RequestFolderEntity.builder()
        .id(folder.getId())
        .userId(folder.getUserId())
        .name(folder.getName())
        .createdOn(folder.getCreatedOn())
        .build();
  }

  private Folder toDomain(RequestFolderEntity entity) {
    return new Folder(
        entity.getId(),
        entity.getUserId(),
        entity.getName(),
        entity.getCreatedOn()
    );
  }

}
