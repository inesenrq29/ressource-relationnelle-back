package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ienrique.ressourceRelationnelle.dto.ChangePasswordDto;
import com.ienrique.ressourceRelationnelle.dto.ForgotPasswordDto;
import com.ienrique.ressourceRelationnelle.dto.ResetPasswordDto;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.entity.PasswordResetToken;
import com.ienrique.ressourceRelationnelle.entity.TokenType;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.repository.AppUserRepository;
import com.ienrique.ressourceRelationnelle.repository.PasswordRepository;
import com.ienrique.ressourceRelationnelle.service.PasswordServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PasswordServiceTest {

  @Mock private PasswordRepository passwordResetTokenRepository;
  @Mock private AppUserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private PasswordServiceImpl passwordService;

  @Nested
  @DisplayName("request password reset")
  class RequestPasswordReset {

    @Test
    @DisplayName("should request password reset")
    void shouldRequestPasswordReset() {
      final ForgotPasswordDto requestDto = new ForgotPasswordDto("email@test.com");
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setMail("email@test.com");

      when(userRepository.findByMail("email@test.com")).thenReturn(Optional.of(user));

      passwordService.requestResetPassword(requestDto);
      verify(userRepository).findByMail("email@test.com");
      verify(passwordResetTokenRepository)
          .deleteByUser_AppUserIdAndType(userId, TokenType.RESET_PASSWORD);
    }
  }

  @Nested
  @DisplayName("reset password")
  class ResetPassword {

    @Test
    @DisplayName("should reset password successfully")
    void shouldResetPassword() {
      final ResetPasswordDto requestDto = new ResetPasswordDto("token", "newPassword");
      final AppUser user = new AppUser();
      user.setHashedPassword("old_hashed_password");
      final PasswordResetToken passwordResetToken = new PasswordResetToken();
      passwordResetToken.setUsed(false);
      passwordResetToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
      passwordResetToken.setUser(user);
      passwordResetToken.setType(TokenType.RESET_PASSWORD);

      when(passwordResetTokenRepository.findByTokenValueAndType("token", TokenType.RESET_PASSWORD))
          .thenReturn(Optional.of(passwordResetToken));
      when(passwordEncoder.encode("newPassword")).thenReturn("new_hashed_password");

      passwordService.resetPassword(requestDto);

      verify(userRepository).save(user);
      verify(passwordResetTokenRepository).save(passwordResetToken);
      verify(passwordEncoder).encode("newPassword");
    }

    @Test
    @DisplayName("should throw bad request token invalid")
    void shouldThrowBadRequestTokenInvalid() {
      final ResetPasswordDto requestDto = new ResetPasswordDto("token", "newPassword");

      when(passwordResetTokenRepository.findByTokenValueAndType("token", TokenType.RESET_PASSWORD))
          .thenReturn(Optional.empty());

      final BadRequestException exception =
          assertThrows(BadRequestException.class, () -> passwordService.resetPassword(requestDto));

      assertEquals("Invalid token", exception.getMessage());
      verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("should throw bad request token already used")
    void shouldThrowBadRequestTokenUsed() {
      final ResetPasswordDto requestDto = new ResetPasswordDto("token", "newPassword");
      final AppUser user = new AppUser();
      user.setHashedPassword("old_hashed_password");
      final PasswordResetToken passwordResetToken = new PasswordResetToken();
      passwordResetToken.setUsed(true);
      passwordResetToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
      passwordResetToken.setUser(user);
      passwordResetToken.setType(TokenType.RESET_PASSWORD);

      when(passwordResetTokenRepository.findByTokenValueAndType("token", TokenType.RESET_PASSWORD))
          .thenReturn(Optional.of(passwordResetToken));

      final BadRequestException exception =
          assertThrows(BadRequestException.class, () -> passwordService.resetPassword(requestDto));

      assertEquals("Token has been already used", exception.getMessage());
      verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("should throw bad request token is expired")
    void shouldThrowBadRequestTokenExpired() {
      final ResetPasswordDto requestDto = new ResetPasswordDto("token", "newPassword");
      final AppUser user = new AppUser();
      user.setHashedPassword("old_hashed_password");
      final PasswordResetToken passwordResetToken = new PasswordResetToken();
      passwordResetToken.setUsed(false);
      passwordResetToken.setExpiresAt(Instant.now().minus(7, ChronoUnit.DAYS));
      passwordResetToken.setUser(user);
      passwordResetToken.setType(TokenType.RESET_PASSWORD);

      when(passwordResetTokenRepository.findByTokenValueAndType("token", TokenType.RESET_PASSWORD))
          .thenReturn(Optional.of(passwordResetToken));

      final BadRequestException exception =
          assertThrows(BadRequestException.class, () -> passwordService.resetPassword(requestDto));

      assertEquals("Token expired", exception.getMessage());
      verify(userRepository, never()).save(any(AppUser.class));
    }
  }

  @Nested
  @DisplayName("change password")
  class ChangePassword {

    @Test
    @DisplayName("should change password successfully")
    void shouldChangePasswordSuccessfully() {
      final UUID userId = UUID.randomUUID();
      final ChangePasswordDto requestDto = new ChangePasswordDto("currentPassword", "newPassword");
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setHashedPassword("current_hashed_password");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("currentPassword", "current_hashed_password")).thenReturn(true);
      when(passwordEncoder.encode("newPassword")).thenReturn("new_hashed_password");

      passwordService.changePassword(userId, requestDto);

      assertEquals("new_hashed_password", user.getHashedPassword());
      verify(userRepository).save(user);
      verify(passwordEncoder).matches("currentPassword", "current_hashed_password");
      verify(passwordEncoder).encode("newPassword");
    }

    @Test
    @DisplayName("should throw bad request new password must be different")
    void shouldThrowBadRequestNewPasswordDifferent() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setHashedPassword("current_hashed_password");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("currentPassword", "current_hashed_password")).thenReturn(true);

      final ChangePasswordDto requestDto =
          new ChangePasswordDto("currentPassword", "currentPassword");

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> passwordService.changePassword(userId, requestDto));

      assertEquals("Passwords must be different", exception.getMessage());
      verify(passwordEncoder, never()).encode(anyString());
      verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("should throw user not found")
    void shouldThrowUserNotFound() {
      final UUID userId = UUID.randomUUID();
      final ChangePasswordDto requestDto = new ChangePasswordDto("currentPassword", "newPassword");

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class, () -> passwordService.changePassword(userId, requestDto));

      assertEquals("User not found", exception.getMessage());
      verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("should throw bad request current password invalid")
    void shouldThrowInvalidPassword() {
      final UUID userId = UUID.randomUUID();
      final ChangePasswordDto requestDto = new ChangePasswordDto("currentPassword", "newPassword");
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setHashedPassword("current_hashed_password");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("currentPassword", "current_hashed_password")).thenReturn(false);

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> passwordService.changePassword(userId, requestDto));

      assertEquals("Invalid current password", exception.getMessage());
      verify(userRepository, never()).save(any(AppUser.class));
    }
  }
}
