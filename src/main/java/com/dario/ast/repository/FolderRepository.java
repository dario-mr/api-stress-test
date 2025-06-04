package com.dario.ast.repository;

import com.dario.ast.core.converter.FolderMapper;
import com.dario.ast.core.domain.Folder;
import com.dario.ast.repository.jpa.FolderJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FolderRepository {

  private final FolderJpaRepository jpaRepository;
  private final FolderMapper mapper;

  public Folder save(Folder folder) {
    var entity = jpaRepository.save(mapper.toEntity(folder));
    return mapper.toDomain(entity);
  }

  public void delete(long id) {
    jpaRepository.deleteById(id);
  }

  public List<Folder> findByUserId(long userId) {
    return jpaRepository.findByUserIdOrderByCreatedOn(userId).stream()
        .map(mapper::toDomain)
        .toList();
  }

}
