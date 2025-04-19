package com.dario.ast.core.service;

import com.dario.ast.core.domain.Folder;
import com.dario.ast.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FolderService {

  private final FolderRepository folderRepository;

  public Folder save(Folder folder) {
    return folderRepository.save(folder);
  }

}
