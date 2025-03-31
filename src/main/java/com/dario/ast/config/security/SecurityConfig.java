package com.dario.ast.config.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Configures Spring Security using VaadinWebSecurity helper.
 * <p>
 * VaadinWebSecurity provides basic Vaadin security configuration for the project out of the box. It sets up security
 * rules for a Vaadin application and restricts all URLs except for public resources and internal Vaadin URLs to
 * authenticated user.
 * <p>
 */
@Profile("!dev")
@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

  private static final String LOGIN_URL = "/login";

  private final String[] whitelistPatterns;

  public SecurityConfig(@Value("${security.whitelist.ant-patterns}") final String whitelistPatterns) {
    this.whitelistPatterns = whitelistPatterns.split("\\s*,\\s*");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(whitelistPatterns).permitAll()
        )
        .headers(headers -> headers
            .frameOptions(Customizer.withDefaults()).disable() // allow H2 Console inside iframes
        )
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/h2-console/**") // disable CSRF for H2 Console
        )
        .oauth2Login(oauth2 -> oauth2
            .loginPage(LOGIN_URL).permitAll()
        );

    super.configure(http);
  }

}