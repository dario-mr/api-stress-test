package com.dario.ast.core.service.oauth;

import com.dario.ast.core.service.oauth.tokenrefresh.TokenRefreshStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
@RequiredArgsConstructor
public class AuthTokenManager {

  private final TokenRefreshStrategy securityContextTokenRefresher;
  private final TokenRefreshStrategy databaseTokenRefresher;

  public void refreshTokenIfExpired(HttpServletRequest request, HttpServletResponse response) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      databaseTokenRefresher.refresh(request, response);
    } else {
      securityContextTokenRefresher.refresh(request, response);
    }
  }

}
