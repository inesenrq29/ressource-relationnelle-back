package controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.ienrique.ressourceRelationnelle.controller.UserController;
import com.ienrique.ressourceRelationnelle.dto.DeleteAccountDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.ForbiddenException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.service.UserService;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = RessourceRelationnelleApplication.class)
public class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserService userService;

  @Nested
  @DisplayName("delete account")
  class DeleteAccount {

    @Test
    @DisplayName("Should return 204")
    void shouldReturn204() throws Exception {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      final DeleteAccountDto requestDto = new DeleteAccountDto("password");

      doNothing().when(userService).deleteAccount(userId, requestDto);

      mockMvc
          .perform(
              delete("/api/users/{userId}", userId)
                  .with(jwt()) // without this -> 403 forbidden error
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should throw 404 user not found")
    void shouldThrow404() throws Exception {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      final DeleteAccountDto requestDto = new DeleteAccountDto("password");

      doThrow(new NotFoundException("User not found"))
          .when(userService)
          .deleteAccount(eq(userId), any(DeleteAccountDto.class));

      mockMvc
          .perform(
              delete("/api/users/{userId}", userId)
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should throw 403 forbidden")
    void shouldThrow403() throws Exception {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      final DeleteAccountDto requestDto = new DeleteAccountDto("password");

      doThrow(new RuntimeException("Forbidden"))
          .when(userService)
          .deleteAccount(userId, requestDto);

      mockMvc
          .perform(
              delete("/api/users/{userId}", userId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should throw bad request for invalid password")
    void shouldThrow400() throws Exception {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      final DeleteAccountDto requestDto = new DeleteAccountDto("wrong password");

      doThrow(new BadRequestException("Invalid password"))
          .when(userService)
          .deleteAccount(eq(userId), any(DeleteAccountDto.class));

      mockMvc
          .perform(
              delete("/api/users/{userId}", userId)
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("get user by id")
  class GetUserById {

    @Test
    @DisplayName("Should return 200")
    void shouldReturn200() throws Exception {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final UserDto userDto = new UserDto();
      userDto.setAppUserId(userId);

      when(userService.getUserById(userId)).thenReturn(userDto);

      mockMvc
          .perform(
              get("/api/users/{userId}", userId).with(jwt()).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should throw 403 forbidden")
    void shouldThrow403() throws Exception {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      final UserDto userDto = new UserDto();
      userDto.setAppUserId(userId);

      doThrow(new ForbiddenException("Forbidden")).when(userService).getUserById(userId);

      mockMvc
          .perform(
              get("/api/users/{userId}", userId).with(jwt()).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users/{userId} -> 404 when not found")
    void getUserById_shouldReturn404() throws Exception {
      UUID userId = UUID.randomUUID();
      when(userService.getUserById(userId)).thenThrow(new NotFoundException("User not found"));

      mockMvc
          .perform(get("/api/users/{userId}", userId).with(jwt()))
          .andExpect(status().isNotFound());
    }
  }
}
