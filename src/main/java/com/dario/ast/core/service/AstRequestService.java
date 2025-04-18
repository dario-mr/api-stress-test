package com.dario.ast.core.service;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Folder;
import com.dario.ast.core.domain.RequestType;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.repository.AstRequestRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AstRequestService {

  private final AstRequestRepository astRequestRepository;

  public List<AstRequest> getByUserIdAndTypeAndStatus(long userId, RequestType requestType, boolean active) {
    return astRequestRepository.findByUserIdAndTypeAndStatus(userId, requestType, active);
  }

  public List<AstRequest> getByUserIdAndType(long userId, RequestType requestType) {
    return astRequestRepository.findByUserIdAndType(userId, requestType);
  }

  public Map<Folder, List<AstRequest>> getFoldersByUserIdAndTypeAndStatus(
      long userId, RequestType requestType, boolean active) {
    return astRequestRepository.getFoldersByUserIdAndTypeAndStatus(userId, requestType, active);
  }

  public Optional<AstRequest> getById(Long id) {
    return astRequestRepository.findById(id);
  }

  public void updateConfigParams(ConfigParams configParams) {
    astRequestRepository.updateConfigParams(configParams);
  }

  public void updateRunParams(RunParams runParams) {
    astRequestRepository.updateRunParams(runParams);
  }

  public Long create(AstRequest astRequest) {
    return astRequestRepository.create(astRequest);
  }

  public void delete(long id) {
    astRequestRepository.delete(id);
  }

  public void deleteFolder(long folderId) {
    astRequestRepository.deleteFolder(folderId);
  }

}
