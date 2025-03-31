package com.dario.ast.repository;

import static java.util.stream.Collectors.toMap;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RequestHeader;
import com.dario.ast.core.domain.RequestQueryParam;
import com.dario.ast.core.domain.RequestUriVariable;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.repository.jpa.AstRequestJpaRepository;
import com.dario.ast.repository.jpa.entity.RequestEntity;
import com.dario.ast.repository.jpa.entity.RequestHeaderEntity;
import com.dario.ast.repository.jpa.entity.RequestQueryParameterEntity;
import com.dario.ast.repository.jpa.entity.RequestUriVariableEntity;
import java.time.Instant;
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
public class AstRequestRepository {

  private final AstRequestJpaRepository jpaRepository;

  public List<AstRequest> findByUserId(long userId) {
    return jpaRepository.findByUserIdOrderByCreatedOn(userId).stream()
        .map(this::mapToDomain)
        .toList();
  }

  public Optional<AstRequest> findById(Long id) {
    return jpaRepository.findById(id)
        .map(this::mapToDomain);
  }

  public Long create(AstRequest astRequest) {
    var now = Instant.now();

    var entity = mapToEntity(astRequest);
    entity.setId(null);
    entity.setCreatedOn(now);
    entity.setModifiedOn(now);

    return jpaRepository.save(entity).getId();
  }

  public void update(AstRequest astRequest) {
    var oldEntity = jpaRepository.findById(astRequest.getConfigParams().getRequestId()).orElseThrow();
    var newEntity = mapToEntity(astRequest);
    newEntity.setCreatedOn(oldEntity.getCreatedOn());
    newEntity.setModifiedOn(Instant.now());

    jpaRepository.save(newEntity);
  }

  public void delete(long id) {
    jpaRepository.deleteById(id);
  }

  private RequestEntity mapToEntity(AstRequest astRequest) {
    var configParams = astRequest.getConfigParams();
    var runParams = astRequest.getRunParams();

    return RequestEntity.builder()
        .id(configParams.getRequestId())
        .name(configParams.getRequestName())
        .userId(configParams.getUserId())
        .uri(configParams.getUri())
        .httpMethod(configParams.getMethod().toString())
        .headers(mapHeadersToEntity(configParams.getHeaders()))
        .uriVariables(mapUriVariablesToEntity(configParams.getUriVariables()))
        .queryParams(mapQueryParamsToEntity(configParams.getQueryParams()))
        .requestBody(configParams.getRequestBody())
        .numRequests(runParams.getNumRequests())
        .threadPoolSize(runParams.getThreadPoolSize())
        .stopOnError(runParams.isStopOnError())
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

  private AstRequest mapToDomain(RequestEntity entity) {
    return new AstRequest(
        mapToConfigParams(entity),
        mapToRunParams(entity));
  }

  private ConfigParams mapToConfigParams(RequestEntity requestEntity) {
    return ConfigParams.builder()
        .requestId(requestEntity.getId())
        .requestName(requestEntity.getName())
        .userId(requestEntity.getUserId())
        .uri(requestEntity.getUri())
        .method(HttpMethod.valueOf(requestEntity.getHttpMethod().toUpperCase()))
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
