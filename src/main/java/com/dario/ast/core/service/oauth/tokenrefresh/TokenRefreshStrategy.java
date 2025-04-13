package com.dario.ast.core.service.oauth.tokenrefresh;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenRefreshStrategy {

  void refresh(HttpServletRequest request, HttpServletResponse response);

}
