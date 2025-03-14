package com.dario.ast.core.service;

import com.dario.ast.core.domain.User;
import com.dario.ast.repository.AstUserRepository;
import com.dario.ast.view.component.notification.ErrorNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AstUserService {

  private final AstUserRepository astUserRepository;
  private final UserSessionService userSessionService;

  public User getCurrentUser() {
    var currentGoogleUser = userSessionService.getUser();
    try {
      return getOrCreateUser(currentGoogleUser.email());
    } catch (Exception ex) {
      log.error("Error getting current user from DB", ex);
      ErrorNotification.show(ex.getMessage());
      throw new RuntimeException(ex);
    }
  }

  private User getOrCreateUser(String userEmail) {
    var optUser = astUserRepository.findByEmail(userEmail);
    if (optUser.isEmpty()) {
      log.info("User with email [{}] not found, creating new one", userEmail);
      return astUserRepository.create(userEmail);
    }

    var user = optUser.get();
    if (!user.isActive()) {
      log.warn("User with email [{}] is not active, will not proceed with fetching user data", userEmail);
      throw new IllegalStateException("User with email %s is not active!".formatted(userEmail));
    }

    return new User(user.getId(), user.getEmail(), user.getCreatedOn(), user.isActive());
  }
}
