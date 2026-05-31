package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ienrique.ressourceRelationnelle.dto.AuthTokenDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.entity.RefreshToken;
import com.ienrique.ressourceRelationnelle.entity.Role;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.mapper.UserMapper;
import com.ienrique.ressourceRelationnelle.repository.RefreshTokenRepository;
import com.ienrique.ressourceRelationnelle.service.JwtServiceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

  @Mock private RefreshTokenRepository tokenRepository;
  @Mock private UserMapper userMapper;

  @InjectMocks private JwtServiceImpl jwtService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jwtService, "jwtSecret", "abcdefghijklmnopqrstuvwxyz123456");
    ReflectionTestUtils.setField(jwtService, "accessTtlMinutes", 15L);
    ReflectionTestUtils.setField(jwtService, "refreshTtlDays", 7L);
  }

  @Nested
  @DisplayName("generate access token")
  class GenerateAccessToken {
    @Test
    @DisplayName("should generate access token")
    void shouldGenerateAccessToken() {
      final Role role = new Role();
      role.setRoleName("USER");
      final AppUser user = new AppUser();
      user.setAppUserId(UUID.randomUUID());
      user.setMail("test@mail.com");
      user.setRole(role);

      final String token = jwtService.generateAccessToken(user);

      assertNotNull(token);
      assertFalse(token.isBlank());

      final SecretKey key =
          Keys.hmacShaKeyFor("abcdefghijklmnopqrstuvwxyz123456".getBytes(StandardCharsets.UTF_8));

      final Claims claims =
          Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

      assertEquals(user.getAppUserId().toString(), claims.getSubject());
      assertEquals("test@mail.com", claims.get("email"));
      assertEquals("USER", claims.get("role"));
      assertNotNull(claims.getIssuedAt());
      assertNotNull(claims.getExpiration());
      assertTrue(claims.getExpiration().after(new Date()));
    }
  }

  @Nested
  @DisplayName("generate refresh token")
  class GenerateRefreshToken {
    @Test
    @DisplayName("should generate refresh token")
    void shouldGenerateRefreshToken() {
      final String ipAddress = "localhost";
      final String userAgent = "Chrome";
      final Role role = new Role();
      role.setRoleName("USER");
      final AppUser user = new AppUser();
      user.setAppUserId(UUID.randomUUID());
      user.setMail("test@mail.com");
      user.setRole(role);

      final String rawToken = jwtService.generateRefreshToken(user, ipAddress, userAgent);

      assertNotNull(rawToken);
      assertFalse(rawToken.isBlank());
      assertEquals(128, rawToken.length());

      final ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
      verify(tokenRepository, times(1)).save(captor.capture());

      final RefreshToken savedToken = captor.getValue();

      assertNotNull(savedToken.getHashedToken());
      assertNotEquals(rawToken, savedToken.getHashedToken());
      assertEquals(user, savedToken.getAppUser());
      assertEquals(ipAddress, savedToken.getIpAddress());
      assertEquals(userAgent, savedToken.getUserAgent());
      assertFalse(savedToken.isRevoked());
      assertNotNull(savedToken.getExpiresAt());
      assertTrue(savedToken.getExpiresAt().isAfter(Instant.now().plus(6, ChronoUnit.DAYS)));
    }
  }

  @Nested
  @DisplayName("revoke token")
  class RevokeToken {
    @Test
    @DisplayName("should throw bad request when token is null")
    void shouldThrowBadRequestWhenTokenIsNull() {
      assertThrows(BadRequestException.class, () -> jwtService.revokeToken(null));
    }

    @Test
    @DisplayName("should throw bad request when token is blank")
    void shouldThrowBadRequestWhenTokenIsBlank() {
      assertThrows(BadRequestException.class, () -> jwtService.revokeToken("   "));
    }

    @Test
    @DisplayName("should revoke token and save it")
    void shouldRevokeAndSaveToken() {
      final String rawToken = "valid-refresh-token";
      final String hashedToken = sha256(rawToken);

      final RefreshToken refreshToken = new RefreshToken();
      refreshToken.setRevoked(false);

      when(tokenRepository.findByHashedToken(hashedToken)).thenReturn(Optional.of(refreshToken));

      jwtService.revokeToken(rawToken);

      assertTrue(refreshToken.isRevoked());
      verify(tokenRepository).save(refreshToken);
    }

    @Test
    @DisplayName("should revoke token already revoked but not save token")
    void shouldNotSaveWhenTokenAlreadyRevoked() {
      final String rawToken = "valid-refresh-token";
      final String hashedToken = sha256(rawToken);

      final RefreshToken refreshToken = new RefreshToken();
      refreshToken.setRevoked(true);

      when(tokenRepository.findByHashedToken(hashedToken)).thenReturn(Optional.of(refreshToken));

      jwtService.revokeToken(rawToken);

      verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("should do nothing when token does not exist")
    void shouldDoNothingWhenTokenDoesNotExist() {
      final String rawToken = "unknown-token";

      when(tokenRepository.findByHashedToken(sha256(rawToken))).thenReturn(Optional.empty());

      jwtService.revokeToken(rawToken);

      verify(tokenRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("validate refresh token")
  class ValidateRefreshToken {
    @Test
    @DisplayName("should throw bad request when refresh token is null")
    void shouldThrowBadRequestWhenRefreshTokenIsNull() {
      assertThrows(BadRequestException.class, () -> jwtService.validateRefreshToken(null));
    }

    @Test
    @DisplayName("should throw bad request when refresh token is blank")
    void shouldThrowBadRequestWhenRefreshTokenIsBlank() {
      assertThrows(BadRequestException.class, () -> jwtService.validateRefreshToken(" "));
    }

    @Test
    @DisplayName("should throw bad request when token doesn't exist")
    void shouldThrowBadRequestWhenTokenDoesNotExist() {
      final String rawToken = "unknown-token";
      when(tokenRepository.findByHashedToken(sha256(rawToken))).thenReturn(Optional.empty());

      final BadRequestException ex =
          assertThrows(BadRequestException.class, () -> jwtService.validateRefreshToken(rawToken));

      assertEquals("Invalid refresh token", ex.getMessage());
    }

    @Test
    @DisplayName("should throw bad request when token is revoked")
    void shouldThrowBadRequestWhenTokenIsRevoked() {
      final String rawToken = "revoked-token";
      final RefreshToken refreshToken = new RefreshToken();
      refreshToken.setRevoked(true);
      refreshToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

      when(tokenRepository.findByHashedToken(sha256(rawToken)))
          .thenReturn(Optional.of(refreshToken));

      final BadRequestException ex =
          assertThrows(BadRequestException.class, () -> jwtService.validateRefreshToken(rawToken));

      assertEquals("Refresh token is revoked", ex.getMessage());
    }

    @Test
    @DisplayName("should throw bad request when token is expired")
    void shouldThrowBadRequestWhenTokenIsExpired() {
      final String rawToken = "expired-token";
      final RefreshToken refreshToken = new RefreshToken();
      refreshToken.setRevoked(false);
      refreshToken.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));

      when(tokenRepository.findByHashedToken(sha256(rawToken)))
          .thenReturn(Optional.of(refreshToken));

      final BadRequestException ex =
          assertThrows(BadRequestException.class, () -> jwtService.validateRefreshToken(rawToken));

      assertEquals("Refresh token is expired", ex.getMessage());
    }

    @Test
    @DisplayName("should validate refresh token")
    void shouldValidateRefreshToken() {
      final String rawToken = "valid-token";
      final RefreshToken refreshToken = new RefreshToken();
      refreshToken.setRevoked(false);
      refreshToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
      final AppUser user = new AppUser();
      refreshToken.setAppUser(user);

      when(tokenRepository.findByHashedToken(sha256(rawToken)))
          .thenReturn(Optional.of(refreshToken));

      final RefreshToken result = jwtService.validateRefreshToken(rawToken);

      assertNotNull(result);
      assertEquals(refreshToken, result);
    }
  }

  @Nested
  @DisplayName("rotate refresh token")
  class RotateRefreshToken {
    @Test
    @DisplayName("should rotate refresh token")
    void shouldRevokeCurrentTokenAndReturnNewTokens() {
      final String rawToken = "current-valid-token";
      final String ipAddress = "localhost";
      final String userAgent = "Chrome";

      final RefreshToken currentToken = new RefreshToken();
      final Role role = new Role();
      role.setRoleId(UUID.randomUUID());
      role.setRoleName("USER");
      final AppUser user = new AppUser();
      user.setAppUserId(UUID.randomUUID());
      user.setRole(role);
      currentToken.setAppUser(user);
      currentToken.setRevoked(false);
      currentToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

      final UserDto userDto = new UserDto();
      when(userMapper.toDto(user)).thenReturn(userDto);
      when(tokenRepository.findByHashedToken(sha256(rawToken)))
          .thenReturn(Optional.of(currentToken));

      final AuthTokenDto result = jwtService.rotateRefreshToken(rawToken, ipAddress, userAgent);

      assertNotNull(result);
      assertNotNull(result.getAccessToken());
      assertNotNull(result.getRefreshToken());
      assertNotNull(result.getUserDto());
      assertEquals(userDto, result.getUserDto());

      assertTrue(currentToken.isRevoked());

      verify(tokenRepository, times(2)).save(any(RefreshToken.class));
      verify(userMapper).toDto(user);
    }
  }

  private String sha256(String rawToken) {
    try {
      final java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
      final byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return java.util.HexFormat.of().formatHex(hash);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
