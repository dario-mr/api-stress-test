package com.dario.ast.core.service;

import static com.dario.ast.core.domain.Folder.defaultFolder;
import static com.dario.ast.util.EventUtil.folderCreated;

import com.dario.ast.core.domain.Folder;
import com.dario.ast.repository.FolderRepository;
import com.dario.ast.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FolderService {

  private final FolderRepository folderRepository;
  private final RequestRepository requestRepository;

  public void createFolder(long userId, Folder parentFolder) {
    var folder = defaultFolder(userId, parentFolder);

    var newFolder = this.save(folder);
    folderCreated(newFolder);
  }

  public Folder save(Folder folder) {
    return folderRepository.save(folder);
  }

  public void delete(long folderId) {
    requestRepository.deleteByFolderId(folderId);
    folderRepository.delete(folderId);
  }

}
