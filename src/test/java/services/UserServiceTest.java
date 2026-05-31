package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.ienrique.ressourceRelationnelle.dto.CreateAccountDto;
import com.ienrique.ressourceRelationnelle.dto.DeleteAccountDto;
import com.ienrique.ressourceRelationnelle.dto.RoleDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateUserDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.entity.AccountStatus;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.entity.Role;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.RoleMapper;
import com.ienrique.ressourceRelationnelle.mapper.UserMapper;
import com.ienrique.ressourceRelationnelle.repository.AppUserRepository;
import com.ienrique.ressourceRelationnelle.repository.PasswordRepository;
import com.ienrique.ressourceRelationnelle.service.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private AppUserRepository userRepository;
  @Mock private PasswordRepository passwordResetTokenRepository;
  @Mock private UserMapper userMapper;
  @Mock private RoleMapper roleMapper;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;
  @Mock private Jwt jwt;

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
    @DisplayName("Should not update pseudo when request pseudo is blank")
    void shouldNotUpdatePseudoWhenBlank() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setPseudo("oldPseudo");

      final UpdateUserDto userRequestDto = new UpdateUserDto("");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      userService.updateUser(userId, userRequestDto);

      assertEquals("oldPseudo", user.getPseudo());
      verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should not update pseudo when request pseudo is null")
    void shouldNotUpdatePseudoWhenNull() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      user.setPseudo("oldPseudo");

      final UpdateUserDto userRequestDto = new UpdateUserDto(null);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      userService.updateUser(userId, userRequestDto);

      assertEquals("oldPseudo", user.getPseudo());
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

  @Nested
  @DisplayName("create account with role")
  class CreateAccountWithRole {

    @Test
    @DisplayName("should create account with role")
    void shouldCreateAccountWithRole() {
      final UUID roleId = UUID.randomUUID();

      final CreateAccountDto createAccountDto = new CreateAccountDto();
      createAccountDto.setPseudo("  john_doe  ");
      createAccountDto.setMail("john_doe@test.com");
      createAccountDto.setPassword("Password123");

      final RoleDto roleDto = new RoleDto();
      roleDto.setRoleId(roleId);
      roleDto.setRoleName("ADMIN");
      createAccountDto.setRole(roleDto);

      final Role role = new Role();
      role.setRoleId(roleId);
      role.setRoleName("ADMIN");

      final AppUser savedUser = new AppUser();
      savedUser.setPseudo("john_doe");
      savedUser.setMail("john_doe@test.com");
      savedUser.setStatus(AccountStatus.ACTIVE);
      savedUser.setRole(role);
      savedUser.setCreatedAt(Instant.now());
      savedUser.setUpdatedAt(Instant.now());

      final UserDto expectedDto = new UserDto();
      expectedDto.setPseudo("john_doe");
      expectedDto.setMail("john_doe@test.com");
      expectedDto.setAppUserIsActive(true);

      when(roleMapper.toEntity(roleDto)).thenReturn(role);
      when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);
      when(userMapper.toDto(savedUser)).thenReturn(expectedDto);

      final UserDto result = userService.createAccountWithRole(createAccountDto);

      assertNotNull(result);
      assertEquals("john_doe", result.getPseudo());

      final ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
      verify(userRepository).save(userCaptor.capture());

      final AppUser userToSave = userCaptor.getValue();
      assertEquals("john_doe", userToSave.getPseudo());
      assertEquals(AccountStatus.ACTIVE, userToSave.getStatus());
      assertEquals(role, userToSave.getRole());
      assertNotNull(userToSave.getCreatedAt());
      assertNotNull(userToSave.getUpdatedAt());

      verify(roleMapper).toEntity(roleDto);
      verify(userMapper).toDto(savedUser);
    }

    @Test
    @DisplayName("should throw when payload is null")
    void shouldThrowWhenPayloadIsNull() {
      final BadRequestException exception =
          assertThrows(BadRequestException.class, () -> userService.createAccountWithRole(null));

      assertEquals("Create account payload is required", exception.getMessage());

      verify(userRepository, never()).save(any(AppUser.class));
      verify(roleMapper, never()).toEntity(any(RoleDto.class));
      verify(userMapper, never()).toDto(any(AppUser.class));
    }

    @Test
    @DisplayName("should throw when pseudo is null")
    void shouldThrowWhenPseudoIsNull() {
      final CreateAccountDto createAccountDto = new CreateAccountDto();
      createAccountDto.setPseudo(null);
      createAccountDto.setMail("john_doe@test.com");
      createAccountDto.setPassword("Password123");

      final RoleDto roleDto = new RoleDto();
      roleDto.setRoleId(UUID.randomUUID());
      roleDto.setRoleName("ADMIN");
      createAccountDto.setRole(roleDto);

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> userService.createAccountWithRole(createAccountDto));

      assertEquals("Pseudo is required", exception.getMessage());

      verify(userRepository, never()).save(any(AppUser.class));
      verify(roleMapper, never()).toEntity(any(RoleDto.class));
      verify(userMapper, never()).toDto(any(AppUser.class));
    }

    @Test
    @DisplayName("should throw when pseudo is blank")
    void shouldThrowWhenPseudoIsBlank() {
      final CreateAccountDto createAccountDto = new CreateAccountDto();
      createAccountDto.setPseudo("   ");
      createAccountDto.setMail("john_doe@test.com");
      createAccountDto.setPassword("Password123");

      final RoleDto roleDto = new RoleDto();
      roleDto.setRoleId(UUID.randomUUID());
      roleDto.setRoleName("ADMIN");
      createAccountDto.setRole(roleDto);

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> userService.createAccountWithRole(createAccountDto));

      assertEquals("Pseudo is required", exception.getMessage());

      verify(userRepository, never()).save(any(AppUser.class));
      verify(roleMapper, never()).toEntity(any(RoleDto.class));
      verify(userMapper, never()).toDto(any(AppUser.class));
    }

    @Test
    @DisplayName("should throw when role is null")
    void shouldThrowWhenRoleIsNull() {
      final CreateAccountDto createAccountDto = new CreateAccountDto();
      createAccountDto.setPseudo("john_doe");
      createAccountDto.setMail("john_doe@test.com");
      createAccountDto.setPassword("Password123");
      createAccountDto.setRole(null);

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> userService.createAccountWithRole(createAccountDto));

      assertEquals("Role is required", exception.getMessage());

      verify(userRepository, never()).save(any(AppUser.class));
      verify(roleMapper, never()).toEntity(any(RoleDto.class));
      verify(userMapper, never()).toDto(any(AppUser.class));
    }
  }

  @Nested
  @DisplayName("get current user")
  class GetCurrentUser {

    @BeforeEach
    void setUp() {
      SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
      SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("should get current user")
    void shouldGetCurrentUser() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      final UserDto userDto = new UserDto();

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(jwt);
      when(jwt.getSubject()).thenReturn(String.valueOf(userId));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userMapper.toDto(user)).thenReturn(userDto);

      final UserDto result = userService.getCurrentUser();

      assertEquals(userDto, result);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
      final UUID userId = UUID.randomUUID();

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(jwt);
      when(jwt.getSubject()).thenReturn(String.valueOf(userId));
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(NotFoundException.class, () -> userService.getCurrentUser());

      assertEquals("User not found", exception.getMessage());
    }
  }
}
