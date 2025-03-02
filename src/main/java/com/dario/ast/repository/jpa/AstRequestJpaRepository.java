package com.dario.ast.repository.jpa;

import com.dario.ast.repository.jpa.entity.AstRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AstRequestJpaRepository extends JpaRepository<AstRequestEntity, Long> {

    List<AstRequestEntity> findByUser_EmailOrderByCreatedOn(String userEmail);
}
