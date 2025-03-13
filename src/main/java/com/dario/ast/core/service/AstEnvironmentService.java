package com.dario.ast.core.service;

import com.dario.ast.core.domain.Environment;
import com.dario.ast.repository.AstEnvironmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AstEnvironmentService {

  private final AstEnvironmentRepository astEnvironmentRepository;

  public List<Environment> getByEmail(String email) {
    return astEnvironmentRepository.findByUserEmail(email);
  }

}
