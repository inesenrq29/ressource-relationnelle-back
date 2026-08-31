package com.ienrique.ressourceRelationnelle.service;

import java.util.List;
import java.util.UUID;

import com.ienrique.ressourceRelationnelle.dto.CreateAccountDto;
import com.ienrique.ressourceRelationnelle.dto.DeleteAccountDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateUserDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.entity.AccountStatus;

public interface UserService {
  void deleteAccount(UUID userId, DeleteAccountDto deleteAccount);

  UserDto getUserById(UUID userId);

  void updateUserStatus(UUID userId, AccountStatus status);

  UserDto createAccountWithRole(CreateAccountDto createAccount);

  void updateUser(UUID userId, UpdateUserDto user);

  UserDto getCurrentUser();

  List<UserDto> getAllUsers();
}
