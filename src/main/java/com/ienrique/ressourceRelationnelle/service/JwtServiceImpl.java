package com.ienrique.ressourceRelationnelle.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HexFormat;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ienrique.ressourceRelationnelle.dto.AuthTokenDto;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.entity.RefreshToken;
import com.ienrique.ressourceRelationnelle.mapper.UserMapper;
import com.ienrique.ressourceRelationnelle.repository.RefreshTokenRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserMapper userMapper;
  private final SecureRandom secureRandom = new SecureRandom();

  @Value("${security.jwt.secret}")
  private String jwtSecret;

  @Value("${security.jwt.access-token-ttl-minutes:15}")
  private long accessTtlMinutes;

  @Value("${security.jwt.refresh-token-ttl-days:7}")
  private long refreshTtlDays;

  @Override
  public String generateAccessToken(AppUser user) {
    final Instant now = Instant.now();
    final Instant expiration = now.plus(accessTtlMinutes, ChronoUnit.MINUTES);

    // génération d'une clé secrète
    final SecretKey key =
        Keys.hmacShaKeyFor(
            jwtSecret.getBytes(
                StandardCharsets
                    .UTF_8)); // on prend le secret en entrée et on le transforme en SecretKey

    // construction du token
    return Jwts.builder()
        .setSubject(user.getAppUserId().toString()) // subject = qui est l'utilisateur de ce token
        .claim("email", user.getMail()) // infos supplémentaires du token (ici email et tole)
        .claim("role", user.getRole().getRoleName())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(expiration))
        .signWith(
            key,
            SignatureAlgorithm
                .HS256) // signature du token permettant de rendre le token unique et infalsifiable
        .compact(); // le token est compilé en chaine de caractère
  }

  @Override
  @Transactional
  public String generateRefreshToken(AppUser user, String ipAddress, String userAgent) {

    // génère une chaine de caractère longue
    final String rawToken = generateSecureToken();

    // hash le mot de passe
    final String hashedToken = hashToken(rawToken);

    // construction du token
    final RefreshToken refreshToken = new RefreshToken();
    refreshToken.setHashedToken(hashedToken);
    refreshToken.setAppUser(user);
    refreshToken.setExpiresAt(Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS));
    refreshToken.setIpAddress(ipAddress);
    refreshToken.setRevoked(false);
    refreshToken.setUserAgent(userAgent);

    refreshTokenRepository.save(refreshToken);

    return rawToken;
  }

  @Override
  @Transactional
  public void revokeToken(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      throw new RuntimeException("Token is required");
    }

    // révoque le token
    refreshTokenRepository
        .findByHashedToken(hashToken(rawToken))
        .ifPresent(
            refreshToken -> {
              if ((!refreshToken.isRevoked())) {
                refreshToken.setRevoked(true);
                refreshTokenRepository.save(refreshToken);
              }
            });
  }

  @Override
  @Transactional
  public RefreshToken validateRefreshToken(String rawToken) {
    // on vérifie que le token n'est ni null ni vide
    if (rawToken == null || rawToken.isBlank()) {
      throw new RuntimeException("Refresh token is required");
    }

    // on véririe que le token existe
    final RefreshToken refreshToken =
        refreshTokenRepository
            .findByHashedToken(hashToken(rawToken))
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

    // on vérifie que le token n'est pas révoqué
    if (refreshToken.isRevoked()) {
      throw new RuntimeException("Refresh token is revoked");
    }

    // on vérifie que le token n'est pas expiré
    if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
      throw new RuntimeException("Refresh token is expired");
    }

    // valide le token si toutes les conditions sont requises
    return refreshToken;
  }

  @Override
  @Transactional
  public AuthTokenDto rotateRefreshToken(String rawToken, String ipAddress, String userAgent) {
    // on révoque le token actuel
    final RefreshToken currentToken = validateRefreshToken(rawToken);
    final AppUser user = currentToken.getAppUser();
    currentToken.setRevoked(true);
    refreshTokenRepository.save(currentToken);

    // on génère un nouvel access token
    final String newAccessToken = generateAccessToken(user);

    // on génère un nouveau refresh token
    final String newRefreshToken = generateRefreshToken(user, ipAddress, userAgent);

    return new AuthTokenDto(newAccessToken, newRefreshToken, userMapper.toDto(user));
  }

  private String hashToken(String rawToken) {
    try {
      // on crée un objet permettant de hasher avec SHA-256
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      // on transforme le token brut en tableau de bytes puis on calcule le hash
      final byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      // on convertit le hash en chaine hexadécimale
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not supported", e);
    }
  }

  private String generateSecureToken() {
    // on crée d'un tableau de bytes de taille 64
    final byte[] bytes = new byte[64];
    // on remplit le tableau avec des valeurs aléatoires sécurisées
    secureRandom.nextBytes(bytes);
    // on convertit en chaîne hexadécimale pour stockage et transmission
    return HexFormat.of().formatHex(bytes);
  }
}
