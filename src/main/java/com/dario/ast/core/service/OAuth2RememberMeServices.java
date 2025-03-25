package com.dario.ast.core.service;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

public class OAuth2RememberMeServices extends PersistentTokenBasedRememberMeServices {

  public OAuth2RememberMeServices(
      String key, UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
    super(key, userDetailsService, tokenRepository);
  }

  @Override
  protected UserDetails processAutoLoginCookie(
      String[] cookieTokens, HttpServletRequest request, HttpServletResponse response) {
    // todo error here
    UserDetails userDetails = super.processAutoLoginCookie(cookieTokens, request, response);

    // Convert the remembered user back to an OAuth2 authentication token
    var oauth2User = new DefaultOAuth2User(
        createAuthorityList("ROLE_USER"),
        Map.of(
            "email", userDetails.getUsername()
        ),
        "email"
    );

    var authentication = new OAuth2AuthenticationToken(
        oauth2User,
        oauth2User.getAuthorities(),
        "google"
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    return userDetails;
  }

}



