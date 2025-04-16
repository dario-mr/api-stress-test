package com.dario.ast.core.service.security;

import com.dario.ast.core.domain.AppCookie;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

@Service
@RequiredArgsConstructor
public class CookieService {

  private final EncryptionService encryptionService;

  public void encryptAndSaveCookie(AppCookie appCookie, String value, HttpServletResponse response) {
    var encryptedValue = encryptionService.encrypt(value);

    var cookie = new Cookie(appCookie.getName(), encryptedValue);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setAttribute("SameSite", "Lax");
    cookie.setPath("/");
    cookie.setMaxAge((int) Duration.ofDays(400).getSeconds());

    response.addCookie(cookie);
  }

  public String getAndDecryptCookie(AppCookie appCookie, HttpServletRequest request) {
    var encryptedValue = WebUtils.getCookie(request, appCookie.getName());
    if (encryptedValue == null) {
      return null;
    }

    return encryptionService.decrypt(encryptedValue.getValue());
  }

  public void deleteCookie(AppCookie appCookie, HttpServletResponse response) {
    var cookie = new Cookie(appCookie.getName(), null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

}
