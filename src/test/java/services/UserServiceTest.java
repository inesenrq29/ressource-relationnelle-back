package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
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

import com.ienrique.ressourceRelationnelle.dto.DeleteAccountDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateUserDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.entity.AccountStatus;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.UserMapper;
import com.ienrique.ressourceRelationnelle.repository.AppUserRepository;
import com.ienrique.ressourceRelationnelle.repository.PasswordRepository;
import com.ienrique.ressourceRelationnelle.service.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private AppUserRepository userRepository;
  @Mock private PasswordRepository passwordResetTokenRepository;
  @Mock private UserMapper userMapper;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserServiceImpl userService;

  // DELETE ACCOUNT METHOD
  @Nested
  @DisplayName("delete account")
  class DeleteAccount {

    @Test
    @DisplayName("Should delete user account")
    void shouldDeleteUserAccount() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setHashedPassword("hashed_password");
      user.setUpdatedAt(Instant.now());

      final DeleteAccountDto requestDto = new DeleteAccountDto("password");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("password", "hashed_password")).thenReturn(true);

      passwordResetTokenRepository.deleteByAppUser_AppUserId(userId);
      userService.deleteAccount(userId, requestDto);

      verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Should throw user not found exception")
    void shouldThrowUserNotFoundException() {
      final UUID userId = UUID.randomUUID();
      final DeleteAccountDto requestDto = new DeleteAccountDto("password");

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> userService.deleteAccount(userId, requestDto));
      verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw invalid password error")
    void shouldThrowInvalidPassword() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setHashedPassword("hashed_password");
      user.setUpdatedAt(Instant.now());

      final DeleteAccountDto requestDto = new DeleteAccountDto("password");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("password", "hashed_password")).thenReturn(false);

      assertThrows(BadRequestException.class, () -> userService.deleteAccount(userId, requestDto));
      verify(userRepository, never()).delete(any());
    }
  }

  // GET USER BY ID METHOD

  @Nested
  @DisplayName("get user by id")
  class GetUserById {

    @Test
    @DisplayName("Should return a user according to his id")
    void shouldReturnUser() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final UserDto expectedUser = new UserDto();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userMapper.toDto(user)).thenReturn(expectedUser);

      final UserDto result = userService.getUserById(userId);

      assertEquals(expectedUser, result);
      verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("Should throw user not found")
    void shouldThrowUserNotFound() {
      final UUID userId = UUID.randomUUID();

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
      verifyNoInteractions(userMapper);
    }
  }

  // UPDATE USER METHOD

  @Nested
  @DisplayName("update user")
  class UpdateUser {

    @Test
    @DisplayName("Should update user")
    void shouldUpdateUser() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setPseudo("pseudo");

      final UpdateUserDto userRequestDto = new UpdateUserDto("pseudo");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      userService.updateUser(userId, userRequestDto);

      assertEquals("pseudo", user.getPseudo());
      verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should update user with blank names")
    void shouldUpdateUserWithBlankName() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setPseudo("");

      final UpdateUserDto userRequestDto = new UpdateUserDto("");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      userService.updateUser(userId, userRequestDto);

      assertEquals("", user.getPseudo());
      verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should update user with null names")
    void shouldUpdateUserWithNullNames() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setPseudo(null);

      final UpdateUserDto userRequestDto = new UpdateUserDto(null);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      userService.updateUser(userId, userRequestDto);

      assertNull(user.getPseudo());
      verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw user not found exception")
    void shouldThrowUserNotFound() {
      final UUID userId = UUID.randomUUID();

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class,
          () -> userService.updateUser(userId, new UpdateUserDto("Last name test")));
      verify(userRepository, never()).save(any());
    }
  }

  // UPDATE USER STATUS METHOD

  @Nested
  @DisplayName("update user status")
  class UpdateUserStatus {

    @Test
    @DisplayName("Should update user status")
    void shouldUpdateUserStatus() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setStatus(AccountStatus.DISABLED);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      userService.updateUserStatus(userId, AccountStatus.ACTIVE);

      assertEquals(AccountStatus.ACTIVE, user.getStatus());
      verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw user not found exception")
    void shouldThrowUserNotFound() {
      final UUID userId = UUID.randomUUID();

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class,
          () -> userService.updateUserStatus(userId, AccountStatus.ACTIVE));
      verify(userRepository, never()).save(any());
    }
  }

  // GET ALL USERS METHOD

  @Nested
  @DisplayName("get all users")
  class GetAllUsers {

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() {
      final AppUser user = new AppUser();
      final AppUser user2 = new AppUser();
      final UserDto userDto = new UserDto();
      final UserDto userDto2 = new UserDto();

      when(userRepository.findAll()).thenReturn(List.of(user, user2));
      when(userMapper.toDto(user)).thenReturn(userDto);
      when(userMapper.toDto(user2)).thenReturn(userDto2);

      final List<UserDto> results = userService.getAllUsers();

      assertEquals(2, results.size());
      assertEquals(List.of(userDto, userDto2), results);
      verify(userMapper).toDto(user);
      verify(userMapper).toDto(user2);
    }
  }
}
