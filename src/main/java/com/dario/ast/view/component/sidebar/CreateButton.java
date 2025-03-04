package com.dario.ast.view.component.sidebar;

import com.dario.ast.core.domain.AstRequest;
import com.dario.ast.core.domain.ConfigParams;
import com.dario.ast.core.domain.RunParams;
import com.dario.ast.core.domain.User;
import com.dario.ast.core.service.AstRequestService;
import com.dario.ast.core.service.AstUserService;
import com.dario.ast.core.service.UserSessionService;
import static com.dario.ast.util.EventUtil.asrRequestCreated;
import com.dario.ast.view.component.notification.ErrorNotification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.HttpMethod.GET;

@Slf4j
@UIScope
@SpringComponent
public class CreateButton extends Button {

  private final AstRequestService astRequestService;
  private final UserSessionService userSessionService;
  private final AstUserService astUserService;

  @Autowired
  public CreateButton(AstRequestService astRequestService, UserSessionService userSessionService,
      AstUserService astUserService) {
    this.astRequestService = astRequestService;
    this.userSessionService = userSessionService;
    this.astUserService = astUserService;

    setHeightFull();
    addClassName("create-button");
    setText("+");

    addClickListener(event -> createAsrRequest());
  }

  private void createAsrRequest() {
    var currentUser = getCurrentUser();
    var configParams = defaultConfigParams(currentUser.getId());
    var runParams = defaultRunParams();
    var astRequest = new AstRequest(configParams, runParams);

    try {
      var astRequestId = astRequestService.create(astRequest);
      asrRequestCreated(astRequestId);
    } catch (Exception ex) {
      log.error("Error creating new Request", ex);
      ErrorNotification.show("Error creating Request");
      throw new RuntimeException(ex);
    }
  }

  private User getCurrentUser() {
    var currentGoogleUser = userSessionService.getUser();
    try {
      return astUserService.getOrCreateUser(currentGoogleUser.email());
    } catch (Exception ex) {
      log.error("Error getting current user from DB", ex);
      ErrorNotification.show(ex.getMessage());
      throw new RuntimeException(ex);
    }
  }

  private static ConfigParams defaultConfigParams(Long userId) {
    return ConfigParams.builder()
        .requestName("New Request")
        .user(User.builder().id(userId).build())
        .uri("")
        .method(GET)
        .requestBody("")
        .build();
  }

  private static RunParams defaultRunParams() {
    return RunParams.builder()
        .numRequests(1)
        .threadPoolSize(1)
        .stopOnError(true)
        .build();
  }
}