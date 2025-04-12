package com.dario.ast.repository.jpa;

import com.dario.ast.repository.jpa.entity.OAuthTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthTokenJpaRepository extends JpaRepository<OAuthTokenEntity, String> {

}
