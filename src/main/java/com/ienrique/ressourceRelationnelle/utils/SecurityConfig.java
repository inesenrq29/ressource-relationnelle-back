package com.ienrique.ressourceRelationnelle.utils;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableMethodSecurity
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
    // encode les mdp avec l'algorithme BCrypt
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/api/auth/**").permitAll().anyRequest().authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret) {
    final SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    return NimbusJwtDecoder.withSecretKey(key).build();
  }
}
