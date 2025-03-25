package com.dario.ast.config;

import com.dario.ast.core.domain.OAuthUser;
import com.dario.ast.core.service.OAuth2RememberMeServices;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

/**
 * Configures Spring Security using VaadinWebSecurity helper.
 * <p>
 * VaadinWebSecurity provides basic Vaadin security configuration for the project out of the box. It sets up security
 * rules for a Vaadin application and restricts all URLs except for public resources and internal Vaadin URLs to
 * authenticated user.
 * <p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

  private static final String LOGIN_URL = "/login";

  private final String[] whitelistPatterns;
  private final OAuth2RememberMeServices rememberMeServices;

  public SecurityConfig(
      @Value("${security.whitelist.ant-patterns}") final String whitelistPatterns,
      PersistentTokenRepository persistentTokenRepository
  ) {
    this.whitelistPatterns = whitelistPatterns.split("\\s*,\\s*");

    UserDetailsService userDetailsService = userDetailsService();
    this.rememberMeServices = new OAuth2RememberMeServices(
        "secure-key", userDetailsService, persistentTokenRepository);
    rememberMeServices.setCookieName("remember-me");
    rememberMeServices.setTokenValiditySeconds(2592000); // 30 days
    rememberMeServices.setUseSecureCookie(true);
    rememberMeServices.setAlwaysRemember(true);
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
            .successHandler(oAuth2SuccessHandler(rememberMeServices)) // remember-me handler
        )
        .rememberMe(rememberMe -> rememberMe
            .rememberMeServices(rememberMeServices)
            .key("secure-key")
        );

    super.configure(http);
  }

  public UserDetailsService userDetailsService() {
    return email -> {
      // Retrieve additional attributes from the session (if they exist)
      var session = VaadinServletRequest.getCurrent().getHttpServletRequest().getSession(false);
      var oauthAttributes = (session != null) ?
          (Map<String, Object>) session.getAttribute("OAUTH_USER_DETAILS") :
          Collections.emptyMap();

      return new OAuthUser(
          (String) oauthAttributes.getOrDefault("given_name", ""),
          (String) oauthAttributes.getOrDefault("family_name", ""),
          email,
          (String) oauthAttributes.getOrDefault("picture", "")
      );
    };
  }

  public AuthenticationSuccessHandler oAuth2SuccessHandler(RememberMeServices rememberMeServices) {
    return (request, response, authentication) -> {
      rememberMeServices.loginSuccess(request, response, authentication);

      if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
        request.getSession().setAttribute("OAUTH_USER_DETAILS", Map.of(
            "given_name", principal.getAttribute("given_name"),
            "family_name", principal.getAttribute("family_name"),
            "email", principal.getAttribute("email"),
            "picture", principal.getAttribute("picture")
        ));
      }

      response.sendRedirect("/");
    };
  }

}