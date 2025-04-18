package com.dario.ast.repository;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.Folder;
import com.dario.ast.core.domain.Request;
import com.dario.ast.core.domain.RequestHeader;
import com.dario.ast.core.domain.RequestQueryParam;
import com.dario.ast.core.domain.RequestType;
import com.dario.ast.core.domain.RequestUriVariable;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.repository.jpa.RequestJpaRepository;
import com.dario.ast.repository.jpa.entity.RequestEntity;
import com.dario.ast.repository.jpa.entity.RequestFolderEntity;
import com.dario.ast.repository.jpa.entity.RequestHeaderEntity;
import com.dario.ast.repository.jpa.entity.RequestQueryParameterEntity;
import com.dario.ast.repository.jpa.entity.RequestUriVariableEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RequestRepository {

  private final RequestJpaRepository jpaRepository;

  public Map<Folder, List<Request>> getFoldersByUserIdAndTypeAndStatus(
      long userId, RequestType requestType, boolean active) {
    return jpaRepository.findByUserIdAndRequestTypeAndActiveOrderByCreatedOn(userId, requestType.toString(), active)
        .stream()
        .map(this::mapToDomain)
        .collect(toMap(
            Request::getFolder,
            request -> new ArrayList<>(singletonList(request)), // mutable list
            (list1, list2) -> {
              list1.addAll(list2);
              return list1;
            },
            LinkedHashMap::new
        ));
  }

  public List<Request> findByUserIdAndType(long userId, RequestType requestType) {
    return jpaRepository.findByUserIdAndRequestTypeOrderByCreatedOn(userId, requestType.toString())
        .stream()
        .map(this::mapToDomain)
        .toList();
  }

  public Optional<Request> findById(Long id) {
    return jpaRepository.findById(id)
        .map(this::mapToDomain);
  }

  public Long create(Request request) {
    var now = Instant.now();

    var entity = mapToEntity(request);
    entity.setId(null);
    entity.setCreatedOn(now);
    entity.setModifiedOn(now);

    return jpaRepository.save(entity).getId();
  }

  public void updateConfigParams(ConfigParams configParams) {
    var currentRequest = jpaRepository.findById(configParams.getRequestId()).orElseThrow();

    // only update config params in request
    currentRequest.setName(configParams.getRequestName());
    currentRequest.setUri(configParams.getUri());
    currentRequest.setHttpMethod(configParams.getMethod().toString());
    currentRequest.setRequestType(configParams.getRequestType().toString());
    currentRequest.setActive(configParams.isActive());
    currentRequest.setHeaders(mapHeadersToEntity(configParams.getHeaders()));
    currentRequest.setUriVariables(mapUriVariablesToEntity(configParams.getUriVariables()));
    currentRequest.setQueryParams(mapQueryParamsToEntity(configParams.getQueryParams()));
    currentRequest.setRequestBody(configParams.getRequestBody());
    currentRequest.setModifiedOn(Instant.now());

    jpaRepository.save(currentRequest);
  }

  public void updateRunParams(RunParams runParams) {
    var currentRequest = jpaRepository.findById(runParams.getRequestId()).orElseThrow();

    // only update run params in request
    currentRequest.setNumRequests(runParams.getNumRequests());
    currentRequest.setThreadPoolSize(runParams.getThreadPoolSize());
    currentRequest.setStopOnError(runParams.isStopOnError());
    currentRequest.setModifiedOn(Instant.now());

    jpaRepository.save(currentRequest);
  }

  public void delete(long id) {
    jpaRepository.deleteById(id);
  }

  public void deleteFolder(long folderId) {
    jpaRepository.deleteRequestEntitiesByFolderId(folderId);
  }

  private RequestEntity mapToEntity(Request request) {
    var configParams = request.getConfigParams();
    var runParams = request.getRunParams();

    return RequestEntity.builder()
        .id(configParams.getRequestId())
        .name(configParams.getRequestName())
        .userId(configParams.getUserId())
        .uri(configParams.getUri())
        .httpMethod(configParams.getMethod().toString())
        .requestType(configParams.getRequestType().toString())
        .active(configParams.isActive())
        .headers(mapHeadersToEntity(configParams.getHeaders()))
        .uriVariables(mapUriVariablesToEntity(configParams.getUriVariables()))
        .queryParams(mapQueryParamsToEntity(configParams.getQueryParams()))
        .requestBody(configParams.getRequestBody())
        .numRequests(runParams.getNumRequests())
        .threadPoolSize(runParams.getThreadPoolSize())
        .stopOnError(runParams.isStopOnError())
        .folder(toEntity(request.getFolder()))
        .build();
  }

  private RequestFolderEntity toEntity(Folder folder) {
    return folder == null ? null
        : RequestFolderEntity.builder()
            .id(folder.getId())
            .name(folder.getName())
            .createdOn(folder.getCreatedOn())
            .build();
  }

  private static Map<String, RequestHeaderEntity> mapHeadersToEntity(Map<String, RequestHeader> headers) {
    if (headers == null) {
      return null;
    }

    return headers.entrySet().stream()
        .collect(toMap(
            Entry::getKey,
            entry -> new RequestHeaderEntity(
                entry.getValue().getValue(),
                entry.getValue().getCreatedOn()
            )));
  }

  private static Map<String, RequestUriVariableEntity> mapUriVariablesToEntity(
      Map<String, RequestUriVariable> uriVariables) {
    if (uriVariables == null) {
      return null;
    }

    return uriVariables.entrySet().stream()
        .collect(toMap(
            Entry::getKey,
            entry -> new RequestUriVariableEntity(
                entry.getValue().getValue(),
                entry.getValue().getCreatedOn()
            )));
  }

  private static Map<String, RequestQueryParameterEntity> mapQueryParamsToEntity(
      Map<String, RequestQueryParam> queryParams) {
    if (queryParams == null) {
      return null;
    }

    return queryParams.entrySet().stream()
        .collect(toMap(
            Entry::getKey,
            entry -> new RequestQueryParameterEntity(
                entry.getValue().getValue(),
                entry.getValue().getCreatedOn()
            )));
  }

  private Request mapToDomain(RequestEntity entity) {
    return new Request(
        mapToConfigParams(entity),
        mapToRunParams(entity),
        toDomain(entity.getFolder())
    );
  }

  private Folder toDomain(RequestFolderEntity folderEntity) {
    return folderEntity == null ? null
        : new Folder(
            folderEntity.getId(),
            folderEntity.getName(),
            folderEntity.getCreatedOn()
        );
  }

  private ConfigParams mapToConfigParams(RequestEntity requestEntity) {
    return ConfigParams.builder()
        .requestId(requestEntity.getId())
        .requestName(requestEntity.getName())
        .userId(requestEntity.getUserId())
        .uri(requestEntity.getUri())
        .method(HttpMethod.valueOf(requestEntity.getHttpMethod().toUpperCase()))
        .requestType(RequestType.fromString(requestEntity.getRequestType()))
        .active(requestEntity.getActive())
        .headers(mapHeadersToDomain(requestEntity.getHeaders()))
        .uriVariables(mapUriVariablesToDomain(requestEntity.getUriVariables()))
        .queryParams(mapQueryParamsToDomain(requestEntity.getQueryParams()))
        .requestBody(requestEntity.getRequestBody())
        .build();
  }

  private static LinkedHashMap<String, RequestHeader> mapHeadersToDomain(Map<String, RequestHeaderEntity> headers) {
    return headers.entrySet().stream()
        .collect(toMap(
            Entry::getKey,
            entry -> new RequestHeader(
                entry.getValue().getValue(),
                entry.getValue().getCreatedOn()),
            (e1, e2) -> e1,
            LinkedHashMap::new));
  }

  private static LinkedHashMap<String, RequestUriVariable> mapUriVariablesToDomain(
      Map<String, RequestUriVariableEntity> uriVariables) {
    return uriVariables.entrySet().stream()
        .collect(toMap(
            Entry::getKey,
            entry -> new RequestUriVariable(
                entry.getValue().getValue(),
                entry.getValue().getCreatedOn()),
            (e1, e2) -> e1,
            LinkedHashMap::new));
  }

  private static LinkedHashMap<String, RequestQueryParam> mapQueryParamsToDomain(
      Map<String, RequestQueryParameterEntity> uriVariables) {
    return uriVariables.entrySet().stream()
        .collect(toMap(
            Entry::getKey,
            entry -> new RequestQueryParam(
                entry.getValue().getValue(),
                entry.getValue().getCreatedOn()),
            (e1, e2) -> e1,
            LinkedHashMap::new));
  }

  private RunParams mapToRunParams(RequestEntity requestEntity) {
    return RunParams.builder()
        .requestId(requestEntity.getId())
        .numRequests(requestEntity.getNumRequests())
        .threadPoolSize(requestEntity.getThreadPoolSize())
        .stopOnError(requestEntity.isStopOnError())
        .build();
  }
}
