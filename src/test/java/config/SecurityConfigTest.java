package config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ienrique.ressourceRelationnelle.utils.SecurityConfig;

@WebMvcTest(controllers = SecurityConfigTest.TestController.class)
@Import(SecurityConfig.class)
@TestPropertySource(
    properties = {
      "security.jwt.secret=unit-test-secret-key-with-at-least-32-characters",
      "security.refresh-cookie.secure=false",
      "security.refresh-cookie.same-site=Lax",
      "app.cors.allowed-origins=http://localhost:4200"
    })
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private JwtAuthenticationConverter jwtAuthenticationConverter;

  @Test
  void shouldUseBCryptToEncodePassword() {
    final String rawPassword = "StrongPassword123!";

    final String encodedPassword = passwordEncoder.encode(rawPassword);

    assertThat(encodedPassword).isNotEqualTo(rawPassword);
    assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
  }

  @Test
  void shouldConvertRoleClaimIntoSpringAuthority() {
    final Jwt jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "HS256")
            .claim("sub", "user@test.fr")
            .claim("role", "ADMIN")
            .build();

    final JwtAuthenticationToken authentication =
        (JwtAuthenticationToken) jwtAuthenticationConverter.convert(jwt);

    assertThat(authentication).isNotNull();

    final Collection<GrantedAuthority> authorities = authentication.getAuthorities();

    assertThat(authorities)
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_ADMIN");
  }

  @Test
  void shouldNotCreateAuthorityWhenRoleClaimIsBlank() {
    final Jwt jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "HS256")
            .claim("sub", "user@test.fr")
            .claim("role", " ")
            .build();

    final JwtAuthenticationToken authentication =
        (JwtAuthenticationToken) jwtAuthenticationConverter.convert(jwt);

    assertThat(authentication).isNotNull();
    assertThat(authentication.getAuthorities()).isEmpty();
  }

  @Test
  void shouldAllowPublicResourceEndpointWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/resources")).andExpect(status().isOk());
  }

  @Test
  void shouldAllowCsrfEndpointWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/csrf")).andExpect(status().isOk());
  }

  @Test
  void shouldRejectProtectedEndpointWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/private")).andExpect(status().isUnauthorized());
  }

  @Test
  void shouldAllowProtectedEndpointWithValidJwt() throws Exception {
    mockMvc
        .perform(
            get("/api/private")
                .with(
                    jwt()
                        .jwt(builder -> builder.claim("role", "USER"))
                        .authorities(() -> "ROLE_USER")))
        .andExpect(status().isOk());
  }

  @Test
  void shouldRejectAuthenticationPostRequestWithoutCsrfToken() throws Exception {
    mockMvc
        .perform(post("/api/auth/test").servletPath("/api/auth/test"))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowAuthenticationPostRequestWithCsrfToken() throws Exception {
    mockMvc.perform(post("/api/auth/test").with(csrf())).andExpect(status().isOk());
  }

  @Test
  void shouldAllowConfiguredCorsOrigin() throws Exception {
    mockMvc
        .perform(
            options("/api/resources")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
        .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
  }

  @Test
  void shouldRejectUnknownCorsOrigin() throws Exception {
    mockMvc
        .perform(
            options("/api/resources")
                .header("Origin", "https://malicious.example")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isForbidden());
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import({SecurityConfig.class, TestController.class})
  static class TestApplication {}

  @RestController
  static class TestController {

    @GetMapping("/api/resources")
    String publicResources() {
      return "resources";
    }

    @GetMapping("/api/csrf")
    String csrf() {
      return "csrf";
    }

    @GetMapping("/api/private")
    String privateEndpoint() {
      return "private";
    }

    @PostMapping("/api/auth/test")
    String authenticationEndpoint() {
      return "authentication";
    }
  }
}
