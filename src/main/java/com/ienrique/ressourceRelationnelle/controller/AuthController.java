package com.ienrique.ressourceRelationnelle.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ienrique.ressourceRelationnelle.dto.AuthResponseDto;
import com.ienrique.ressourceRelationnelle.dto.AuthTokenDto;
import com.ienrique.ressourceRelationnelle.dto.LoginDto;
import com.ienrique.ressourceRelationnelle.dto.RegisterUserDto;
import com.ienrique.ressourceRelationnelle.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponseDto> signUp(
      final @Valid @RequestBody RegisterUserDto requestDto,
      final HttpServletRequest httpRequest,
      final HttpServletResponse httpServletResponse) {
    // initialisation de l'adresse IP et du user agent
    final String ipAddress = httpRequest.getRemoteAddr();
    final String userAgent = httpRequest.getHeader("User-Agent");

    final AuthTokenDto tokenDto = authService.signUp(requestDto, ipAddress, userAgent);

    // ajout du token dans un cookie HttpOnly
    addRefreshTokenCookie(httpServletResponse, tokenDto.getRefreshToken());

    final AuthResponseDto response =
        new AuthResponseDto(tokenDto.getAccessToken(), tokenDto.getUserDto());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponseDto> login(
      final @Valid @RequestBody LoginDto requestDto,
      final HttpServletRequest httpRequest,
      final HttpServletResponse httpServletResponse) {
    // initialisation de l'adresse IP et du user agent
    final String ipAddress = httpRequest.getRemoteAddr();
    final String userAgent = httpRequest.getHeader("User-Agent");

    final AuthTokenDto tokenDto = authService.login(requestDto, ipAddress, userAgent);

    // ajout du token dans un cookie HttpOnly
    addRefreshTokenCookie(httpServletResponse, tokenDto.getRefreshToken());

    final AuthResponseDto response =
        new AuthResponseDto(tokenDto.getAccessToken(), tokenDto.getUserDto());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) {
    // lecture du refresh token
    final String refreshToken = extractRefreshTokenFromCookie(httpServletRequest);

    if (refreshToken != null && !refreshToken.isBlank()) {
      authService.logout(refreshToken);
    }

    // suppression cookie
    clearRefreshTokenCookie(httpServletResponse);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<AuthResponseDto> refreshToken(
      final HttpServletRequest httpRequest, final HttpServletResponse httpServletResponse) {
    // initialisation de l'adresse IP et du user agent
    final String ipAddress = httpRequest.getRemoteAddr();
    final String userAgent = httpRequest.getHeader("User-Agent");

    // lecture du token
    final String refreshToken = extractRefreshTokenFromCookie(httpRequest);

    if (refreshToken == null || refreshToken.isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    final AuthTokenDto tokenDto = authService.refreshToken(refreshToken, ipAddress, userAgent);

    // création du cookie
    addRefreshTokenCookie(httpServletResponse, tokenDto.getRefreshToken());

    final AuthResponseDto response =
        new AuthResponseDto(tokenDto.getAccessToken(), tokenDto.getUserDto());
    return ResponseEntity.ok(response);
  }

  // read refresh token
  private String extractRefreshTokenFromCookie(final HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }

    for (Cookie cookie : request.getCookies()) {
      if ("refreshToken".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }

    return null;
  }

  // create cookie
  private void addRefreshTokenCookie(
      final HttpServletResponse response, final String refreshToken) {
    final ResponseCookie cookie =
        ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(false) // TODO: mettre true en prod
            .path("/")
            .maxAge(Duration.ofDays(7))
            .sameSite("Lax")
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  // delete cookie
  private void clearRefreshTokenCookie(final HttpServletResponse response) {
    final ResponseCookie cookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(false) // TODO: mettre true en prod
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
