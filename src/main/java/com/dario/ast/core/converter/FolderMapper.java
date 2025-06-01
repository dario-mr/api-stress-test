package com.dario.ast.core.converter;

import com.dario.ast.core.domain.Folder;
import com.dario.ast.repository.jpa.entity.RequestFolderEntity;
import org.springframework.stereotype.Component;

@Component
public class FolderMapper implements Mapper<Folder, RequestFolderEntity> {

  @Override
  public Folder toDomain(RequestFolderEntity entity) {
    if (entity == null) {
      return null;
    }

    return new Folder(
        entity.getId(),
        entity.getUserId(),
        entity.getName(),
        entity.getCreatedOn(),
        toDomain(entity.getParentFolder())
    );
  }

  @Override
  public RequestFolderEntity toEntity(Folder domain) {
    if (domain == null) {
      return null;
    }

    RequestFolderEntity entity = new RequestFolderEntity();
    entity.setId(domain.getId());
    entity.setName(domain.getName());
    entity.setCreatedOn(domain.getCreatedOn());
    entity.setUserId(domain.getUserId());
    entity.setParentFolder(toEntity(domain.getParentFolder()));

    return entity;
  }
}
