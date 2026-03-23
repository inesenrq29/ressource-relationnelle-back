package controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.ienrique.ressourceRelationnelle.controller.AuthController;
import com.ienrique.ressourceRelationnelle.dto.AuthTokenDto;
import com.ienrique.ressourceRelationnelle.dto.LoginDto;
import com.ienrique.ressourceRelationnelle.dto.RegisterUserDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.service.AuthService;

import jakarta.servlet.http.Cookie;

@WebMvcTest(AuthController.class)
@ContextConfiguration(classes = RessourceRelationnelleApplication.class)
public class AuthControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AuthService authService;

  @Nested
  @DisplayName("sign up")
  class SignUp {

    @Test
    @DisplayName("Should return 201 account created")
    void shouldReturn201() throws Exception {
      final RegisterUserDto requestDto =
          new RegisterUserDto(
              "email@test.com", "pseudo", "password1234", "password1234", true, true);
      final UserDto userDto = new UserDto();
      final AuthTokenDto tokenDto = new AuthTokenDto("access-token", "refresh-token", userDto);

      when(authService.signUp(any(RegisterUserDto.class), anyString(), anyString()))
          .thenReturn(tokenDto);

      mockMvc
          .perform(
              post("/api/auth/register")
                  .with(jwt())
                  .content(objectMapper.writeValueAsString(requestDto))
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("User-Agent", "test")
                  .with(
                      request -> {
                        request.setRemoteAddr("remoteAddr");
                        return request;
                      }))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should throw 400 if email is invalid")
    void shouldThrow400() throws Exception {
      final RegisterUserDto requestDto =
          new RegisterUserDto("email", "pseudo", "password", "password", true, true);

      doThrow(new BadRequestException("Invalid email format"))
          .when(authService)
          .signUp(eq(requestDto), anyString(), anyString());

      mockMvc
          .perform(
              post("/api/auth/register")
                  .with(jwt())
                  .content(objectMapper.writeValueAsString(requestDto))
                  .contentType(MediaType.APPLICATION_JSON)
                  .header("User-Agent", "test")
                  .with(
                      request -> {
                        request.setRemoteAddr("remoteAddr");
                        return request;
                      }))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("login")
  class Login {

    @Test
    @DisplayName("Should return 200")
    void shouldReturn200() throws Exception {
      final LoginDto requestDto = new LoginDto("email@test.com", "password");
      final UserDto userDto = new UserDto();
      final AuthTokenDto tokenDto = new AuthTokenDto("access-token", "refresh-token", userDto);

      when(authService.login(any(LoginDto.class), anyString(), anyString())).thenReturn(tokenDto);

      mockMvc
          .perform(
              post("/api/auth/login")
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto))
                  .header("User-Agent", "test")
                  .with(
                      request -> {
                        request.setRemoteAddr("remoteAddr");
                        return request;
                      }))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("logout")
  class Logout {

    @Test
    @DisplayName("Should return 204")
    void shouldReturn204() throws Exception {

      doNothing().when(authService).logout("refresh-token");

      mockMvc
          .perform(
              post("/api/auth/logout")
                  .with(jwt())
                  .cookie(new Cookie("refreshToken", "refresh-token")))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should throw 400 refresh token is required")
    void shouldThrow400() throws Exception {

      doThrow(new BadRequestException("Refresh token is required"))
          .when(authService)
          .logout("refresh-token");

      mockMvc
          .perform(
              post("/api/auth/logout")
                  .with(jwt())
                  .cookie(new Cookie("refreshToken", "refresh-token")))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("refresh-token")
  class RefreshToken {

    @Test
    @DisplayName("Should return 200")
    void shouldReturn200() throws Exception {
      final UserDto userDto = new UserDto();
      final AuthTokenDto tokenDto = new AuthTokenDto("access-token", "new-refresh-token", userDto);

      when(authService.refreshToken(eq("refresh-token"), anyString(), anyString()))
          .thenReturn(tokenDto);
      mockMvc
          .perform(
              post("/api/auth/refresh-token")
                  .with(jwt())
                  .cookie(new Cookie("refreshToken", "refresh-token"))
                  .header("User-Agent", "test")
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(
                      request -> {
                        request.setRemoteAddr("remoteAddr");
                        return request;
                      }))
          .andExpect(status().isOk());
    }
  }
}
