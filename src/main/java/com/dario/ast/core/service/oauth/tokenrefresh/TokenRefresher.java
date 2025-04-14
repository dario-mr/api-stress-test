package com.dario.ast.core.service.oauth.tokenrefresh;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenRefresher {

  void refresh(HttpServletRequest request, HttpServletResponse response);

}
