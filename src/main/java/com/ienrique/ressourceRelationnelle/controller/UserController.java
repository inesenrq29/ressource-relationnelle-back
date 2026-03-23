package com.ienrique.ressourceRelationnelle.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ienrique.ressourceRelationnelle.dto.CreateAccountDto;
import com.ienrique.ressourceRelationnelle.dto.DeleteAccountDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateUserDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.entity.AccountStatus;
import com.ienrique.ressourceRelationnelle.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteAccount(
      @PathVariable final UUID userId,
      final @Valid @RequestBody DeleteAccountDto deleteAccountDto) {
    userService.deleteAccount(userId, deleteAccountDto);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{userId}") // TODO: mettre en place PreAuthorize
  public ResponseEntity<UserDto> getUserById(@PathVariable final UUID userId) {
    final UserDto response = userService.getUserById(userId);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{userId}")
  public ResponseEntity<Void> updateUser(
      @PathVariable final UUID userId, final @Valid @RequestBody UpdateUserDto request) {
    userService.updateUser(userId, request);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{userId}/status")
  public ResponseEntity<Void> updateUserStatus(
      @PathVariable final UUID userId, final @RequestBody AccountStatus status) {
    userService.updateUserStatus(userId, status);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {
    final List<UserDto> response = userService.getAllUsers();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  public ResponseEntity<UserDto> getCurrentUser() {
    final UserDto currentUser = userService.getCurrentUser();
    return ResponseEntity.ok(currentUser);
  }

  @PostMapping("/with-role")
  public ResponseEntity<UserDto> createAccountWithRole(
      final @RequestBody @Valid CreateAccountDto createAccountDto) {
    final UserDto createdUser = userService.createAccountWithRole(createAccountDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }
}
