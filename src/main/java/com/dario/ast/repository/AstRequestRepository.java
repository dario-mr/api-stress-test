package com.dario.ast.repository;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.repository.jpa.AstRequestJpaRepository;
import com.dario.ast.repository.jpa.entity.AstRequestEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AstRequestRepository {

    private final AstRequestJpaRepository jpaRepository;

    public List<AstRequest> findByUserEmail(String userEmail) {
        return jpaRepository.findByUser_EmailOrderByCreatedOn(userEmail).stream()
                .map(this::mapToDomain)
                .toList();
    }

    private AstRequest mapToDomain(AstRequestEntity entity) {
        return new AstRequest(
                entity.getId(),
                entity.getName(),
                mapToConfigParams(entity),
                mapToRunParams(entity)
        );
    }

    private ConfigParams mapToConfigParams(AstRequestEntity astRequestEntity) {
        return ConfigParams.builder()
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
}
