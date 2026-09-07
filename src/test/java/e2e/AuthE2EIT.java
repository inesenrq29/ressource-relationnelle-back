package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ienrique.ressourceRelationnelle.RessourceRelationnelleApplication;
import com.ienrique.ressourceRelationnelle.dto.LoginDto;
import com.ienrique.ressourceRelationnelle.dto.RegisterUserDto;

@SpringBootTest(
    classes = RessourceRelationnelleApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Testcontainers
class AuthE2EIT {

  private static final String PASSWORD = "Password123!";

  @Container @ServiceConnection
  static final MariaDBContainer<?> mariaDb =
      new MariaDBContainer<>("mariadb:11.4")
          .withDatabaseName("rr_e2e")
          .withUsername("rrtest")
          .withPassword("rrtest");

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldRegisterAuthenticateAndReadCurrentUserThroughHttpApi() throws Exception {

    final String suffix = UUID.randomUUID().toString().substring(0, 8);
    final String email = "e2e-" + suffix + "@test.local";
    final String pseudo = "e2e-" + suffix;

    final CsrfContext registrationCsrf = fetchCsrf();

    final RegisterUserDto registerRequest =
        new RegisterUserDto(email, pseudo, PASSWORD, PASSWORD, true, true);

    final ResponseEntity<String> registrationResponse =
        postWithCsrf("/api/auth/register", registerRequest, registrationCsrf);

    assertThat(registrationResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    assertThat(registrationResponse.getHeaders().get(HttpHeaders.SET_COOKIE))
        .isNotNull()
        .anyMatch(cookie -> cookie.startsWith("refreshToken="));

    final JsonNode registrationBody = objectMapper.readTree(registrationResponse.getBody());

    final String accessToken = registrationBody.path("accessToken").asText();

    assertThat(accessToken).isNotBlank();

    assertThat(registrationBody.path("userDto").path("mail").asText()).isEqualTo(email);

    final HttpHeaders authenticatedHeaders = new HttpHeaders();

    authenticatedHeaders.setBearerAuth(accessToken);

    final ResponseEntity<String> currentUserResponse =
        restTemplate.exchange(
            "/api/users/me", HttpMethod.GET, new HttpEntity<>(authenticatedHeaders), String.class);

    assertThat(currentUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    final JsonNode currentUserBody = objectMapper.readTree(currentUserResponse.getBody());

    assertThat(currentUserBody.path("mail").asText()).isEqualTo(email);

    assertThat(currentUserBody.path("pseudo").asText()).isEqualTo(pseudo);

    final CsrfContext loginCsrf = fetchCsrf();

    final ResponseEntity<String> loginResponse =
        postWithCsrf("/api/auth/login", new LoginDto(email, PASSWORD), loginCsrf);

    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(objectMapper.readTree(loginResponse.getBody()).path("accessToken").asText())
        .isNotBlank();

    final ResponseEntity<String> healthResponse =
        restTemplate.getForEntity("/actuator/health", String.class);

    assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(objectMapper.readTree(healthResponse.getBody()).path("status").asText())
        .isEqualTo("UP");
  }

  private CsrfContext fetchCsrf() throws Exception {

    final ResponseEntity<String> response = restTemplate.getForEntity("/api/csrf", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    final String token = objectMapper.readTree(response.getBody()).path("token").asText();

    final String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

    assertThat(token).isNotBlank();
    assertThat(setCookie).isNotBlank();

    return new CsrfContext(token, setCookie.split(";", 2)[0]);
  }

  private ResponseEntity<String> postWithCsrf(
      final String path, final Object body, final CsrfContext csrfContext) {

    final HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);

    headers.set("X-XSRF-TOKEN", csrfContext.token());

    headers.add(HttpHeaders.COOKIE, csrfContext.cookie());

    headers.set(HttpHeaders.USER_AGENT, "ressource-relationnelle-e2e-test");

    return restTemplate.exchange(
        path, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
  }

  private record CsrfContext(String token, String cookie) {}
}
