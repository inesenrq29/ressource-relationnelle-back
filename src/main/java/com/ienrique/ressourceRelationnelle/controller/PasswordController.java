package com.ienrique.ressourceRelationnelle.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ienrique.ressourceRelationnelle.dto.ChangePasswordDto;
import com.ienrique.ressourceRelationnelle.dto.ForgotPasswordDto;
import com.ienrique.ressourceRelationnelle.dto.ResetPasswordDto;
import com.ienrique.ressourceRelationnelle.service.PasswordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordController {

  private final PasswordService passwordService;

  @PostMapping("/reset-request")
  public ResponseEntity<Void> requestPasswordReset(
      @Valid @RequestBody final ForgotPasswordDto request) {
    passwordService.requestResetPassword(request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/reset")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody final ResetPasswordDto request) {
    passwordService.resetPassword(request);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/users/{userId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or (#userId.toString() == principal.subject)")
  public ResponseEntity<Void> changePassword(
      @PathVariable final UUID userId, @Valid @RequestBody final ChangePasswordDto request) {
    passwordService.changePassword(userId, request);
    return ResponseEntity.noContent().build();
  }
}
