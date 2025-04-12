package com.dario.ast.config.oauth;

import com.dario.ast.core.service.oauth.AuthTokenManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthTokenRefreshFilter extends OncePerRequestFilter {

  private final AuthTokenManager authTokenManager;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      authTokenManager.refreshTokenIfExpired(request, response);
    } catch (Exception e) {
      log.error("Failed to refresh auth token: {}", e.getMessage(), e);
    }

    filterChain.doFilter(request, response);
  }

}
