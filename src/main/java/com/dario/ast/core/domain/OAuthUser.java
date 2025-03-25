package com.dario.ast.core.domain;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record OAuthUser(
    String name,
    String surname,
    String email,
    String pictureUrl
) implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(() -> "ROLE_USER"); // Default role
  }

  @Override
  public String getPassword() {
    return null; // No password needed for OAuth2
  }

  @Override
  public String getUsername() {
    return email;
  }
}
