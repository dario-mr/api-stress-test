package com.dario.ast.repository;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.domain.User;
import com.dario.ast.repository.jpa.AstRequestJpaRepository;
import com.dario.ast.repository.jpa.entity.AstRequestEntity;
import com.dario.ast.repository.jpa.entity.AstUserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AstRequestRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final AstRequestJpaRepository jpaRepository;

    public List<AstRequest> findByUserEmail(String userEmail) {
        return jpaRepository.findByUser_EmailOrderByCreatedOn(userEmail).stream()
                .map(this::mapToDomain)
                .toList();
    }

    // TODO add "New request" button
    public void create(AstRequest astRequest) {
        var now = Instant.now();

        var entity = mapToEntity(astRequest);
        entity.setId(null);
        entity.setCreatedOn(now);
        entity.setModifiedOn(now);

        jpaRepository.save(entity);
    }

    public void update(AstRequest astRequest) {
        var oldEntity = jpaRepository.findById(astRequest.getConfigParams().getRequestId()).orElseThrow();
        var newEntity = mapToEntity(astRequest);
        newEntity.setCreatedOn(oldEntity.getCreatedOn());
        newEntity.setModifiedOn(Instant.now());

        jpaRepository.save(newEntity);
    }

    private AstRequestEntity mapToEntity(AstRequest astRequest) {
        var configParams = astRequest.getConfigParams();
        var runParams = astRequest.getRunParams();

        return AstRequestEntity.builder()
                .id(configParams.getRequestId())
                .name(configParams.getRequestName())
                // only set user reference, without instantiating the whole user entity
                .user(entityManager.getReference(AstUserEntity.class, configParams.getUser().getId()))
                .uri(configParams.getUri())
                .httpMethod(configParams.getMethod().toString())
                .headers(configParams.getHeaders())
                .uriVariables(configParams.getUriVariables())
                .queryParams(configParams.getQueryParams())
                .requestBody(configParams.getRequestBody())
                .numRequests(runParams.getNumRequests())
                .threadPoolSize(runParams.getThreadPoolSize())
                .stopOnError(runParams.isStopOnError())
                .build();
    }

    private AstRequest mapToDomain(AstRequestEntity entity) {
        return new AstRequest(
                mapToConfigParams(entity),
                mapToRunParams(entity)
        );
    }

    private ConfigParams mapToConfigParams(AstRequestEntity astRequestEntity) {
        return ConfigParams.builder()
                .requestId(astRequestEntity.getId())
                .requestName(astRequestEntity.getName())
                .user(mapToDomainUser(astRequestEntity.getUser()))
                .uri(astRequestEntity.getUri())
                .method(HttpMethod.valueOf(astRequestEntity.getHttpMethod().toUpperCase()))
                .headers(astRequestEntity.getHeaders())
                .uriVariables(astRequestEntity.getUriVariables())
                .queryParams(astRequestEntity.getQueryParams())
                .requestBody(astRequestEntity.getRequestBody())
                .build();
    }

    private RunParams mapToRunParams(AstRequestEntity astRequestEntity) {
        return RunParams.builder()
                .numRequests(astRequestEntity.getNumRequests())
                .threadPoolSize(astRequestEntity.getThreadPoolSize())
                .stopOnError(astRequestEntity.isStopOnError())
                .build();
    }

    private User mapToDomainUser(AstUserEntity userEntity) {
        return User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .createdOn(userEntity.getCreatedOn())
                .active(userEntity.isActive())
                .build();
    }
}
