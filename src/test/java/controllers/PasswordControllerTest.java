package controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ienrique.ressourceRelationnelle.RessourceRelationnelleApplication;
import com.ienrique.ressourceRelationnelle.controller.PasswordController;
import com.ienrique.ressourceRelationnelle.dto.ChangePasswordDto;
import com.ienrique.ressourceRelationnelle.dto.ForgotPasswordDto;
import com.ienrique.ressourceRelationnelle.dto.ResetPasswordDto;
import com.ienrique.ressourceRelationnelle.service.PasswordService;

@WebMvcTest(PasswordController.class)
@ContextConfiguration(classes = RessourceRelationnelleApplication.class)
public class PasswordControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private PasswordService passwordService;

  @Nested
  @DisplayName("request password reset")
  class RequestPasswordReset {

    @Test
    @DisplayName("Should return 204 when requesting password reset successfully")
    void shouldRequestPasswordReset() throws Exception {
      final ForgotPasswordDto requestDto = new ForgotPasswordDto("email@test.com");

      doNothing().when(passwordService).requestResetPassword(any(ForgotPasswordDto.class));

      mockMvc
          .perform(
              post("/api/password/reset-request")
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isNoContent());
      verify(passwordService).requestResetPassword(any(ForgotPasswordDto.class));
    }

    @Test
    @DisplayName("Should return 400 when email is invalid")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
      final ForgotPasswordDto requestDto = new ForgotPasswordDto("invalid-email");

      mockMvc
          .perform(
              post("/api/password/reset-request")
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isBadRequest());
      verify(passwordService, never()).requestResetPassword(any(ForgotPasswordDto.class));
    }

    @Test
    @DisplayName("Should return 400 when email is blank")
    void shouldReturn400WhenEmailIsBlank() throws Exception {
      final ForgotPasswordDto requestDto = new ForgotPasswordDto("");

      mockMvc
          .perform(
              post("/api/password/reset-request")
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isBadRequest());
      verify(passwordService, never()).requestResetPassword(any(ForgotPasswordDto.class));
    }
  }

  @Nested
  @DisplayName("reset password")
  class Reset {

    @Test
    @DisplayName("Should return 204 when resetting password successfully")
    void shouldResetPassword() throws Exception {
      final ResetPasswordDto requestDto = new ResetPasswordDto("token", "newPassword12");

      doNothing().when(passwordService).resetPassword(any(ResetPasswordDto.class));

      mockMvc
          .perform(
              post("/api/password/reset")
                  .with(jwt())
                  .content(objectMapper.writeValueAsString(requestDto))
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNoContent());
      verify(passwordService).resetPassword(any(ResetPasswordDto.class));
    }

    @Test
    @DisplayName("Should return 400 when token is blank")
    void shouldReturn400WhenTokenIsBlank() throws Exception {
      final ResetPasswordDto requestDto = new ResetPasswordDto("", "newPassword12");

      mockMvc
          .perform(
              post("/api/password/reset")
                  .with(jwt())
                  .content(objectMapper.writeValueAsString(requestDto))
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
      verify(passwordService, never()).resetPassword(any(ResetPasswordDto.class));
    }

    @Test
    @DisplayName("Should return 400 when new password is too short")
    void shouldReturn400WhenPasswordTooShort() throws Exception {
      final ResetPasswordDto requestDto = new ResetPasswordDto("token", "short");

      mockMvc
          .perform(
              post("/api/password/reset")
                  .with(jwt())
                  .content(objectMapper.writeValueAsString(requestDto))
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
      verify(passwordService, never()).resetPassword(any(ResetPasswordDto.class));
    }

    @Test
    @DisplayName("Should return 400 when new password is blank")
    void shouldReturn400WhenNewPasswordIsBlank() throws Exception {
      final ResetPasswordDto requestDto = new ResetPasswordDto("token", "");

      mockMvc
          .perform(
              post("/api/password/reset")
                  .with(jwt())
                  .content(objectMapper.writeValueAsString(requestDto))
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
      verify(passwordService, never()).resetPassword(any(ResetPasswordDto.class));
    }
  }

  @Nested
  @DisplayName("change password")
  class ChangePassword {

    @Test
    @DisplayName("Should return 204 when changing password successfully")
    void shouldChangePassword() throws Exception {
      final UUID userId = UUID.randomUUID();
      final ChangePasswordDto requestDto =
          new ChangePasswordDto("currentPassword", "newPassword12");

      doNothing().when(passwordService).changePassword(eq(userId), any(ChangePasswordDto.class));

      mockMvc
          .perform(
              put("/api/password/users/{userId}", userId)
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isNoContent());

      verify(passwordService).changePassword(eq(userId), any(ChangePasswordDto.class));
    }

    @Test
    @DisplayName("Should return 403 when not authenticated")
    void shouldReturn403WhenNotAuthenticated() throws Exception {
      final UUID userId = UUID.randomUUID();
      final ChangePasswordDto requestDto =
          new ChangePasswordDto("currentPassword", "newPassword12");

      mockMvc
          .perform(
              put("/api/password/users/{userId}", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isForbidden());
      verify(passwordService, never())
          .changePassword(any(UUID.class), any(ChangePasswordDto.class));
    }

    @Test
    @DisplayName("Should return 400 when current password is blank")
    void shouldReturn400WhenCurrentPasswordIsBlank() throws Exception {
      final UUID userId = UUID.randomUUID();
      final ChangePasswordDto requestDto = new ChangePasswordDto("", "newPassword12");

      mockMvc
          .perform(
              put("/api/password/users/{userId}", userId)
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isBadRequest());
      verify(passwordService, never())
          .changePassword(any(UUID.class), any(ChangePasswordDto.class));
    }

    @Test
    @DisplayName("Should return 400 when new password is too short")
    void shouldReturn400WhenNewPasswordTooShort() throws Exception {
      final UUID userId = UUID.randomUUID();
      final ChangePasswordDto requestDto = new ChangePasswordDto("currentPassword", "short");

      mockMvc
          .perform(
              put("/api/password/users/{userId}", userId)
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isBadRequest());
      verify(passwordService, never())
          .changePassword(any(UUID.class), any(ChangePasswordDto.class));
    }
  }
}
