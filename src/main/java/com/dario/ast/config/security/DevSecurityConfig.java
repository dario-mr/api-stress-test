package com.dario.ast.config.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.GenericFilterBean;

@Profile("dev")
@Configuration
@EnableWebSecurity
public class DevSecurityConfig extends VaadinWebSecurity {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .addFilterBefore(new DevAuthFilter(), BasicAuthenticationFilter.class);

    super.configure(http);
  }

  private static class DevAuthFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws ServletException, IOException {

      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        var mockUser = new DefaultOAuth2User(
            Set.of(new SimpleGrantedAuthority("ROLE_USER")),
            Map.of(
                "sub", "111235512772934408325",
                "given_name", "Dario",
                "family_name", "Mauri",
                "email", "dario.mauri9@gmail.com",
                "picture", "https://lh3.googleusercontent.com/a/ACg8ocIei7A86wJJMtwFWuCKAsAfiodTBXMaJBrc38DROLhcUjXocEDk=s96-c"
            ),
            "sub"
        );

        var auth = new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
      }

      chain.doFilter(request, response);
    }
  }
}
