package com.dario.ast.core.service;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Folder;
import com.dario.ast.core.domain.Request;
import com.dario.ast.core.domain.RequestType;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.repository.RequestRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {

  private final RequestRepository requestRepository;


  public List<Request> getByUserIdAndType(long userId, RequestType requestType) {
    return requestRepository.findByUserIdAndType(userId, requestType);
  }

  public Map<Folder, List<Request>> getFoldersByUserIdAndTypeAndStatus(
      long userId, RequestType requestType, boolean active) {
    return requestRepository.getFoldersByUserIdAndTypeAndStatus(userId, requestType, active);
  }

  public Optional<Request> getById(Long id) {
    return requestRepository.findById(id);
  }

  public void updateConfigParams(ConfigParams configParams) {
    requestRepository.updateConfigParams(configParams);
  }

  public void updateRunParams(RunParams runParams) {
    requestRepository.updateRunParams(runParams);
  }

  public Long create(Request request) {
    return requestRepository.create(request);
  }

  public void delete(long id) {
    requestRepository.delete(id);
  }

  public void deleteFolder(long folderId) {
    requestRepository.deleteFolder(folderId);
  }

}
