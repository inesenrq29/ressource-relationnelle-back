package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ienrique.ressourceRelationnelle.dto.AuthTokenDto;
import com.ienrique.ressourceRelationnelle.dto.LoginDto;
import com.ienrique.ressourceRelationnelle.dto.RegisterUserDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.entity.AccountStatus;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.entity.Role;
import com.ienrique.ressourceRelationnelle.mapper.RoleMapper;
import com.ienrique.ressourceRelationnelle.mapper.UserMapper;
import com.ienrique.ressourceRelationnelle.repository.AppUserRepository;
import com.ienrique.ressourceRelationnelle.repository.RoleRepository;
import com.ienrique.ressourceRelationnelle.service.AuthServiceImpl;
import com.ienrique.ressourceRelationnelle.service.JwtService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
  @Mock private AppUserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private UserMapper userMapper;
  @Mock private RoleMapper roleMapper;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;

  @InjectMocks private AuthServiceImpl authService;

  @Nested
  @DisplayName("sign up")
  class SignUp {

    @Test
    @DisplayName("Should sign up successfully")
    void shouldSignUp() {
      final RegisterUserDto requestDto =
          new RegisterUserDto("john.doe@test.com", "John", "password", "password", true, true);
      final Role role = new Role();
      role.setRoleName("USER");

      when(userRepository.existsByMail(requestDto.getEmail())).thenReturn(false);
      when(roleRepository.findByRoleName(role.getRoleName())).thenReturn(Optional.of(role));
      when(passwordEncoder.encode("password")).thenReturn("hashed_password");

      final AppUser user = new AppUser();
      user.setMail(requestDto.getEmail());
      user.setStatus(AccountStatus.ACTIVE);
      user.setPseudo(requestDto.getPseudo());
      user.setRole(role);
      user.setHashedPassword("hashed_password");

      when(userRepository.save(any(AppUser.class))).thenReturn(user);

      when(jwtService.generateAccessToken(user)).thenReturn("access-token");
      when(jwtService.generateRefreshToken(user, "remoteAddr", "User-Agent"))
          .thenReturn("refresh-token");

      final UserDto userDto = new UserDto();
      when(userMapper.toDto(user)).thenReturn(userDto);

      final AuthTokenDto result = authService.signUp(requestDto, "remoteAddr", "User-Agent");

      final ArgumentCaptor<AppUser> userCaptor =
          ArgumentCaptor.forClass(
              AppUser
                  .class); // captor permet de récupérer user enregistré dans le userRepository.save
      // pour vérifier son contenu (car sans ça -> pas accès)
      verify(userRepository).save(userCaptor.capture());

      final AppUser userSaved = userCaptor.getValue();

      assertEquals("john.doe@test.com", userSaved.getMail());
      assertEquals("John", userSaved.getPseudo());

      assertEquals("access-token", result.getAccessToken());
      assertEquals("refresh-token", result.getRefreshToken());
      assertEquals(result.getUserDto(), userDto);
    }

    @Test
    @DisplayName("Should throw bad request email already exists")
    void shouldThrowEmailExists() {
      final RegisterUserDto requestDto =
          new RegisterUserDto("john.doe@test.com", "John ", "password", "password", true, true);

      when(userRepository.existsByMail(requestDto.getEmail())).thenReturn(true);

      assertThrows(
          RuntimeException.class, () -> authService.signUp(requestDto, "remoteAddr", "User-Agent"));
      verify(userRepository, never()).save(any());
      verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should throw bad request invalid passwords")
    void shouldThrowInvalidPasswords() {
      final RegisterUserDto requestDto =
          new RegisterUserDto("john.doe@test.com", "John ", "password", "pasword", true, true);

      when(userRepository.existsByMail(requestDto.getEmail())).thenReturn(false);

      assertThrows(
          RuntimeException.class, () -> authService.signUp(requestDto, "remoteAddr", "User-Agent"));
      verify(userRepository, never()).save(any());
      verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should throw bad request terms must be accepted")
    void shouldThrowBadRequestTerms() {
      final RegisterUserDto requestDto =
          new RegisterUserDto("john.doe@test.com", "John ", "password", "password", false, true);

      when(userRepository.existsByMail(requestDto.getEmail())).thenReturn(false);

      assertThrows(
          RuntimeException.class, () -> authService.signUp(requestDto, "remoteAddr", "User-Agent"));
      verify(userRepository, never()).save(any());
      verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should throw bad request privacy policy must be accepted")
    void shouldThrowBadRequestPrivacyPolicy() {
      final RegisterUserDto requestDto =
          new RegisterUserDto("john.doe@test.com", "John ", "password", "password", true, false);

      when(userRepository.existsByMail(requestDto.getEmail())).thenReturn(false);

      assertThrows(
          RuntimeException.class, () -> authService.signUp(requestDto, "remoteAddr", "User-Agent"));
      verify(userRepository, never()).save(any());
      verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should throw user role not found")
    void shouldThrowUserRoleNotFound() {
      final RegisterUserDto requestDto =
          new RegisterUserDto("john.doe@test.com", "John ", "password", "password", true, true);

      when(userRepository.existsByMail(requestDto.getEmail())).thenReturn(false);

      assertThrows(
          RuntimeException.class, () -> authService.signUp(requestDto, "remoteAddr", "User-Agent"));
      verify(userRepository, never()).save(any());
      verify(jwtService, never()).generateAccessToken(any());
    }
  }

  @Nested
  @DisplayName("login")
  class Login {

    @Test
    @DisplayName("Should login successfully")
    void shouldLogin() {
      final LoginDto requestDto = new LoginDto("email@test.com", "password");
      final AppUser user = new AppUser();
      user.setHashedPassword("hashed_password");
      user.setStatus(AccountStatus.ACTIVE);

      when(userRepository.findByMail(requestDto.getEmail())).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(requestDto.getPassword(), user.getHashedPassword()))
          .thenReturn(true);

      final AppUser savedUser = new AppUser();
      savedUser.setMail("email@test.com");
      savedUser.setHashedPassword("hashed_password");
      savedUser.setStatus(AccountStatus.ACTIVE);

      when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

      when(jwtService.generateAccessToken(savedUser)).thenReturn("access-token");
      when(jwtService.generateRefreshToken(savedUser, "remoteAddr", "User-Agent"))
          .thenReturn("refresh-token");

      final UserDto userDto = new UserDto();

      when(userMapper.toDto(savedUser)).thenReturn(userDto);

      final AuthTokenDto result = authService.login(requestDto, "remoteAddr", "User-Agent");

      assertEquals("access-token", result.getAccessToken());
      assertEquals("refresh-token", result.getRefreshToken());
      assertEquals(userDto, result.getUserDto());
    }

    @Test
    @DisplayName("Should throw user not found exception")
    void shouldThrowUserNotFound() {
      final LoginDto requestDto = new LoginDto("email@test.com", "password");
      final AppUser user = new AppUser();
      user.setHashedPassword("hashed_password");
      user.setStatus(AccountStatus.ACTIVE);

      when(userRepository.findByMail(requestDto.getEmail())).thenReturn(Optional.empty());

      assertThrows(
          RuntimeException.class, () -> authService.login(requestDto, "remoteAddr", "User-Agent"));
      verify(userRepository, never()).save(any());
      verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should throw bad request account disabled")
    void shouldThrowAccountDisabled() {
      final LoginDto requestDto = new LoginDto("email@test.com", "password");
      final AppUser user = new AppUser();
      user.setHashedPassword("hashed_password");
      user.setStatus(AccountStatus.DISABLED);

      when(userRepository.findByMail(requestDto.getEmail())).thenReturn(Optional.of(user));

      assertThrows(
          RuntimeException.class, () -> authService.login(requestDto, "remoteAddr", "User-Agent"));
      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw bad request invalid password")
    void shouldThrowInvalidPassword() {
      final LoginDto requestDto = new LoginDto("email@test.com", "password");
      final AppUser user = new AppUser();
      user.setHashedPassword("wrong_hashed_password");
      user.setStatus(AccountStatus.ACTIVE);

      when(userRepository.findByMail(requestDto.getEmail())).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(requestDto.getPassword(), user.getHashedPassword()))
          .thenReturn(false);

      assertThrows(
          RuntimeException.class, () -> authService.login(requestDto, "remoteAddr", "User-Agent"));
      verify(userRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("logout")
  class Logout {

    @Test
    @DisplayName("Should logout successfully")
    void shouldLogout() {
      authService.logout("refresh-token");

      verify(jwtService).revokeToken("refresh-token");
    }

    @Test
    @DisplayName("Should throw bad request refresh token is required")
    void shouldThrowErrorTokenNull() {

      assertThrows(RuntimeException.class, () -> authService.logout(null));

      verify(jwtService, never()).revokeToken(anyString());
    }

    @Test
    @DisplayName("Should throw bad request refresh token is required")
    void shouldThrowErrorTokenBlank() {

      assertThrows(RuntimeException.class, () -> authService.logout(""));

      verify(jwtService, never()).revokeToken(anyString());
    }

    @Test
    @DisplayName("Should throw bad request logout request is null")
    void shouldThrowErrorTRequest() {

      assertThrows(RuntimeException.class, () -> authService.logout(null));

      verify(jwtService, never()).revokeToken(anyString());
    }
  }

  @Nested
  @DisplayName("refresh token")
  class RefreshToken {

    @Test
    @DisplayName("Should rotate refresh token")
    void shouldRotate() {
      final UserDto userDto = new UserDto();
      final AuthTokenDto rotatedToken =
          new AuthTokenDto("new-access-token", "new-refresh-token", userDto);

      when(jwtService.rotateRefreshToken("refresh-token", "remoteAddr", "User-Agent"))
          .thenReturn(rotatedToken);

      final AuthTokenDto result =
          authService.refreshToken("refresh-token", "remoteAddr", "User-Agent");

      verify(jwtService).rotateRefreshToken("refresh-token", "remoteAddr", "User-Agent");
      assertEquals("new-access-token", result.getAccessToken());
      assertEquals("new-refresh-token", result.getRefreshToken());
      assertEquals(userDto, result.getUserDto());
    }

    @Test
    @DisplayName("Should throw bad request refresh token is required")
    void shouldThrowErrorTokenNull() {

      assertThrows(
          RuntimeException.class, () -> authService.refreshToken(null, "remoteAddr", "User-Agent"));

      verify(jwtService, never()).rotateRefreshToken(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw bad request refresh token is required")
    void shouldThrowErrorTokenBlank() {

      assertThrows(
          RuntimeException.class, () -> authService.refreshToken("", "remoteAddr", "User-Agent"));

      verify(jwtService, never()).rotateRefreshToken(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw bad request logout request is null")
    void shouldThrowErrorTRequest() {

      assertThrows(
          RuntimeException.class, () -> authService.refreshToken(null, "remoteAddr", "User-Agent"));

      verify(jwtService, never()).rotateRefreshToken(anyString(), anyString(), anyString());
    }
  }
}
