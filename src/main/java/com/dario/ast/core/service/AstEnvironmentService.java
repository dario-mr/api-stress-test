package com.dario.ast.core.service;

import com.dario.ast.core.domain.AppState;
import com.dario.ast.core.domain.Environment;
import com.dario.ast.repository.AstEnvironmentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AstEnvironmentService {

  private final AstEnvironmentRepository astEnvironmentRepository;
  private final AppState appState;

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

  public void updateEnvironmentInAppState(Environment updatedEnvironment) {
    var currentEnvironments = new ArrayList<>(appState.getEnvironments());

    currentEnvironments.replaceAll(environment ->
        Objects.equals(environment.getId(), updatedEnvironment.getId())
            ? updatedEnvironment
            : environment
    );

    // update user environments
    appState.setEnvironments(currentEnvironments);

    // if the currently selected environment was updated, update it in the app state
    if (appState.getSelectedEnvironment().equals(updatedEnvironment)) {
      appState.setSelectedEnvironment(updatedEnvironment);
    }
  }

}
