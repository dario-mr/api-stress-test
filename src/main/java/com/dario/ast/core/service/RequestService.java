package com.dario.ast.core.service;

import static com.dario.ast.core.domain.ConfigParams.defaultConfigParams;
import static com.dario.ast.core.domain.RunParams.defaultRunParams;
import static com.dario.ast.util.EventUtil.requestCreated;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Folder;
import com.dario.ast.core.domain.Request;
import com.dario.ast.core.domain.RequestType;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.repository.RequestRepository;
import java.util.List;
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

  public Optional<Request> getById(Long id) {
    return requestRepository.findById(id);
  }

  public void updateConfigParams(ConfigParams configParams) {
    requestRepository.updateConfigParams(configParams);
  }

  public void updateRunParams(RunParams runParams) {
    requestRepository.updateRunParams(runParams);
  }

  public void updateFolder(long requestId, Folder folder) {
    requestRepository.updateFolder(requestId, folder);
  }

  public Request create(Request request) {
    return requestRepository.create(request);
  }

  public void createDefaultRequestAndNotify(long userId, Folder parentFolder) {
    var configParams = defaultConfigParams(userId);
    var runParams = defaultRunParams();
    var request = new Request(configParams, runParams, parentFolder);

    var newRequest = create(request);
    requestCreated(newRequest);
  }

  public void duplicateRequestAndNotify(Request request) {
    var duplicatedRequest = request.duplicate();
    duplicatedRequest.getConfigParams().setRequestName(request.getConfigParams().getRequestName() + " Copy");

    var newRequest = create(duplicatedRequest);
    requestCreated(newRequest);
  }

  public void delete(long id) {
    requestRepository.delete(id);
  }

}
