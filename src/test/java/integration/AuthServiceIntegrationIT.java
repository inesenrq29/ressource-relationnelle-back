package integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ienrique.ressourceRelationnelle.RessourceRelationnelleApplication;
import com.ienrique.ressourceRelationnelle.dto.AuthTokenDto;
import com.ienrique.ressourceRelationnelle.dto.RegisterUserDto;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.repository.AppUserRepository;
import com.ienrique.ressourceRelationnelle.repository.RefreshTokenRepository;
import com.ienrique.ressourceRelationnelle.repository.RoleRepository;
import com.ienrique.ressourceRelationnelle.service.AuthService;

@SpringBootTest(classes = RessourceRelationnelleApplication.class)
@ActiveProfiles("integration")
@Testcontainers
@Transactional
class AuthServiceIntegrationIT {

  private static final String EMAIL = "integration@test.local";
  private static final String PASSWORD = "Password123!";

  @Container @ServiceConnection
  static final MariaDBContainer<?> mariaDb =
      new MariaDBContainer<>("mariadb:11.4")
          .withDatabaseName("rr_integration")
          .withUsername("rrtest")
          .withPassword("rrtest");

  @Autowired private AuthService authService;

  @Autowired private AppUserRepository appUserRepository;

  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void shouldRegisterUserAndPersistAuthenticationDataInMariaDb() {

    final RegisterUserDto request = registrationRequest(EMAIL, "integration-user");

    final AuthTokenDto result = authService.signUp(request, "127.0.0.1", "integration-test");

    assertThat(result.getAccessToken()).isNotBlank();

    assertThat(result.getRefreshToken()).isNotBlank();

    assertThat(result.getUserDto().getMail()).isEqualTo(EMAIL);

    final AppUser persistedUser = appUserRepository.findByMail(EMAIL).orElseThrow();

    assertThat(persistedUser.getRole().getRoleName()).isEqualTo("USER");

    assertThat(passwordEncoder.matches(PASSWORD, persistedUser.getHashedPassword())).isTrue();

    assertThat(
            refreshTokenRepository.findByAppUser_AppUserIdAndRevokedFalse(
                persistedUser.getAppUserId()))
        .hasSize(1);

    assertThat(roleRepository.findByRoleName("USER")).isPresent();
  }

  @Test
  void shouldRejectDuplicateEmailUsingRealDatabaseConstraintFlow() {

    authService.signUp(
        registrationRequest(EMAIL, "integration-user"), "127.0.0.1", "integration-test");

    assertThatThrownBy(
            () ->
                authService.signUp(
                    registrationRequest(EMAIL, "another-user"), "127.0.0.1", "integration-test"))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Email already exists");
  }

  private RegisterUserDto registrationRequest(final String email, final String pseudo) {

    return new RegisterUserDto(email, pseudo, PASSWORD, PASSWORD, true, true);
  }
}
