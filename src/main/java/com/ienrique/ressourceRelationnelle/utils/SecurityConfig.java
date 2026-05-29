package com.ienrique.ressourceRelationnelle.utils;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private static final String ROLE_PREFIX = "ROLE_";

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() { // extracts roles
    final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter(); // transform JWT claims into GrantedAuthority

    grantedAuthoritiesConverter.setAuthoritiesClaimName("role"); // Read "role" claim

    grantedAuthoritiesConverter.setAuthorityPrefix(ROLE_PREFIX); // add prefix ROLE_

    final JwtAuthenticationConverter converter =
        new JwtAuthenticationConverter(); // transform JWT into Authentication
    converter.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          final Object role = jwt.getClaim("role"); // reading claim
          if (role instanceof String r && !r.isBlank()) { // test if claim is null blank or empty
            return java.util.List.of(
                new SimpleGrantedAuthority(
                    ROLE_PREFIX + r)); // transform into authority ADMIN into ROLE_ADMIN
          }
          return java.util.List.of();
        });

    return converter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    // encode password with BCrypt algorithm
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(
            AbstractHttpConfigurer
                ::disable) // disable CSRF protection (useless with JWT cause no cookies)
        .sessionManagement(
            sm ->
                sm.sessionCreationPolicy(
                    SessionCreationPolicy
                        .STATELESS)) // API stateless : no stcoked session in servor, all is in the
        // JWT
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.POST, "/api/password/reset-request", "/api/password/reset")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/resources", "/api/resources/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()) // other request need authent
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt ->
                        jwt.jwtAuthenticationConverter(
                            jwtAuthenticationConverter()))); // verify JWT and transform role for
    // spring security
    return http.build(); // build and return security configuration
  }

  @Bean
  public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret) {
    final SecretKeySpec key =
        new SecretKeySpec(secret.getBytes(), "HmacSHA256"); // create secret key from yaml value
    return NimbusJwtDecoder.withSecretKey(key).build(); // decode signed JWT with HMAC SHA256
  }
}
