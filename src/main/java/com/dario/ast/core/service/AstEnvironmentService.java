package com.dario.ast.core.service;

import com.dario.ast.core.domain.Environment;
import com.dario.ast.repository.AstEnvironmentRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AstEnvironmentService {

  private final AstEnvironmentRepository astEnvironmentRepository;

  public List<Environment> getByUserId(long userId) {
    return astEnvironmentRepository.findByUserId(userId);
  }

  public Optional<Environment> getById(Long id) {
    return astEnvironmentRepository.findById(id);
  }

  public Long create(Environment environment) {
    return astEnvironmentRepository.create(environment);
  }

  public void update(Environment environment) {
    astEnvironmentRepository.update(environment);
  }

  public void delete(long id) {
    astEnvironmentRepository.delete(id);
  }

}
