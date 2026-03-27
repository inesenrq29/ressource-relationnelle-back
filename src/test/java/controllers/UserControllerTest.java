package controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import com.ienrique.ressourceRelationnelle.dto.CreateAccountDto;
import com.ienrique.ressourceRelationnelle.dto.DeleteAccountDto;
import com.ienrique.ressourceRelationnelle.dto.RoleDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateUserDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.entity.AccountStatus;
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

      doThrow(new ForbiddenException("Forbidden"))
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

  @Nested
  @DisplayName("update user")
  class UpdateUser {

    @Test
    @DisplayName("should update user")
    void shouldUpdateUser() throws Exception {
      final UUID userId = UUID.randomUUID();
      final UpdateUserDto request = new UpdateUserDto();
      request.setPseudo("updated pseudo");

      userService.updateUser(userId, request);

      mockMvc
          .perform(
              put("/api/users/{userId}", userId)
                  .content(objectMapper.writeValueAsString(request))
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(jwt()))
          .andExpect(status().isNoContent());
    }
  }

  @Nested
  @DisplayName("update user status")
  class UpdateUserStatus {

    @Test
    @DisplayName("should update user status")
    void shouldUpdateUserStatus() throws Exception {
      final UUID userId = UUID.randomUUID();

      userService.updateUserStatus(userId, AccountStatus.DISABLED);

      mockMvc
          .perform(
              patch("/api/users/{userId}/status", userId)
                  .with(jwt())
                  .content(objectMapper.writeValueAsString(AccountStatus.DISABLED))
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNoContent());
    }
  }

  @Nested
  @DisplayName("get all users")
  class GetAllUsers {

    @Test
    @DisplayName("should get users")
    void shouldGetUsers() throws Exception {
      final UserDto user1 = new UserDto();
      final UserDto user2 = new UserDto();

      when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

      mockMvc
          .perform(get("/api/users").with(jwt()).contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("get current user")
  class GetCurrentUser {

    @Test
    @DisplayName("should return current user")
    void shouldReturnCurrentUser() throws Exception {
      final UserDto userDto = new UserDto();

      when(userService.getCurrentUser()).thenReturn(userDto);

      mockMvc
          .perform(get("/api/users/me").with(jwt()).contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("create account with role")
  class CreateAccountWithRole {

    @Test
    @DisplayName("should create account with role")
    void shouldCreateAccountWithRole() throws Exception {
      final CreateAccountDto request = new CreateAccountDto();
      final RoleDto role = new RoleDto();
      role.setRoleId(UUID.randomUUID());
      role.setRoleName("SUPER-ADMIN");
      request.setPseudo("new account");
      request.setRole(role);
      final UserDto userDto = new UserDto();

      when(userService.createAccountWithRole(request)).thenReturn(userDto);

      mockMvc
          .perform(
              post("/api/users/with-role")
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());
    }
  }
}
