package com.dario.ast.repository;

import com.dario.ast.repository.jpa.AstRequestJpaRepository;
import com.dario.ast.repository.jpa.entity.AstRequestEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AstRequestRepository {

    private final AstRequestJpaRepository jpaRepository;

    // TODO map to AstRequest, adding necessary fields (name, ID?)
    public List<AstRequestEntity> findByUserEmail(String userEmail) {
        return jpaRepository.findByUser_Email(userEmail);
    }
}
