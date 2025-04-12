package com.dario.ast.config.security;

import com.dario.ast.repository.OAuthTokenRepository;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;

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
  private final ClientRegistrationRepository clientRegistrationRepository;
  private final OAuth2AuthorizedClientService authorizedClientService;
  private final OAuthTokenRefreshFilter oauthTokenRefreshFilter;
  private final OAuthTokenRepository oauth2TokenRepository;

  public SecurityConfig(
      @Value("${security.whitelist.ant-patterns}") final String whitelistPatterns,
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientService authorizedClientService,
      OAuthTokenRefreshFilter oauthTokenRefreshFilter,
      OAuthTokenRepository oauth2TokenRepository) {
    this.whitelistPatterns = whitelistPatterns.split("\\s*,\\s*");
    this.clientRegistrationRepository = clientRegistrationRepository;
    this.authorizedClientService = authorizedClientService;
    this.oauthTokenRefreshFilter = oauthTokenRefreshFilter;
    this.oauth2TokenRepository = oauth2TokenRepository;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // TODO refactor to external services
    http
        .addFilterAfter(oauthTokenRefreshFilter, SecurityContextHolderFilter.class)
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
            .authorizationEndpoint(authorization -> authorization
                .authorizationRequestResolver(authRequestResolver())
            )
            .loginPage(LOGIN_URL).permitAll()
            .successHandler(oAuth2SuccessHandler())
        );

    super.configure(http);
  }

  @Bean
  public OAuth2AuthorizationRequestResolver authRequestResolver() {
    var defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository,
        "/oauth2/authorization");

    return new OAuth2AuthorizationRequestResolver() {
      @Override
      public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        var authRequest = defaultResolver.resolve(request);
        if (authRequest == null) {
          return null;
        }

        // asking google oauth2 to provide offline access, in order to make it produce a refresh_token
        var additionalParams = new HashMap<>(authRequest.getAdditionalParameters());
        additionalParams.put("prompt", "consent");
        additionalParams.put("access_type", "offline");

        return OAuth2AuthorizationRequest.from(authRequest)
            .additionalParameters(additionalParams)
            .build();
      }

      @Override
      public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return defaultResolver.resolve(request, clientRegistrationId); // fallback to default behavior
      }
    };
  }

  @Bean
  public AuthenticationSuccessHandler oAuth2SuccessHandler() {
    return (request, response, authentication) -> {
      var oauthToken = (OAuth2AuthenticationToken) authentication;
      var userId = oauthToken.getName();
      saveCookie("userId", userId, response);

      var client = authorizedClientService.loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), userId);

      var refreshToken = client.getRefreshToken();
      if (refreshToken != null) {
        oauth2TokenRepository.save(userId, refreshToken.getTokenValue(), client.getAccessToken().getExpiresAt());
      }

      response.sendRedirect("/");
    };
  }

  // todo move to CookieService with encryption, define cookie name as constant/enum
  private void saveCookie(String name, String value, HttpServletResponse response) {
    var cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setAttribute("SameSite", "Lax");
    cookie.setPath("/");
    cookie.setMaxAge((int) Duration.ofDays(400).getSeconds());

    response.addCookie(cookie);
  }

}