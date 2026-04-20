package com.ienrique.ressourceRelationnelle.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ienrique.ressourceRelationnelle.dto.CreateAccountDto;
import com.ienrique.ressourceRelationnelle.dto.DeleteAccountDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateUserDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.entity.AccountStatus;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.RoleMapper;
import com.ienrique.ressourceRelationnelle.mapper.UserMapper;
import com.ienrique.ressourceRelationnelle.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final AppUserRepository userRepository;
  private final UserMapper userMapper;
  private final RoleMapper roleMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void deleteAccount(UUID userId, DeleteAccountDto deleteAccount) {
    // vérifie que le user existe
    final AppUser user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    // vérifie que le mot de passe hashé entré est le même que le mot de passe hashé en BDD
    if (!passwordEncoder.matches(deleteAccount.getPassword(), user.getHashedPassword())) {
      throw new BadRequestException("Invalid password");
    }

    // suppression de l'utilisateur
    userRepository.delete(user);
  }

  @Override
  public UserDto getUserById(UUID userId) {
    // vérifie que le user existe
    final AppUser user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    // mappe de l'utilisateur actuellement entité en dto pour pouvoir l'envoyer au front
    return userMapper.toDto(user);
  }

  @Override
  public void updateUserStatus(UUID userId, AccountStatus status) {
    // vérifie que le user existe
    final AppUser user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    // met à jour le statut
    user.setStatus(status);

    // met à jour la date de modification
    user.setUpdatedAt(Instant.now());

    // enregistre en BDD
    userRepository.save(user);
  }

  @Override
  @Transactional
  public UserDto createAccountWithRole(CreateAccountDto createAccount) {
    // vérifie que les champs ne sont pas null
    if (createAccount == null) {
      throw new BadRequestException("Create account payload is required");
    }

    // vérifie que le pseudo ni n'est null ni vide
    if (createAccount.getPseudo() == null || createAccount.getPseudo().isBlank()) {
      throw new BadRequestException("Pseudo is required");
    }

    // vérifie que le role n'est pas null
    if (createAccount.getRole() == null) {
      throw new BadRequestException("Role is required");
    }

    final AppUser user = new AppUser();

    // création du pseudo
    user.setPseudo(createAccount.getPseudo().trim());
    // création du statut
    user.setStatus(AccountStatus.ACTIVE);
    user.setCreatedAt(Instant.now());
    user.setUpdatedAt(Instant.now());
    user.setHashedPassword(passwordEncoder.encode(createAccount.getPassword()));
    user.setMail(createAccount.getMail());

    // ajout du role
    user.setRole(roleMapper.toEntity(createAccount.getRole()));

    // enregistrement en base de données
    final AppUser savedUser = userRepository.save(user);

    return userMapper.toDto(savedUser);
  }

  @Override
  public void updateUser(UUID userId, UpdateUserDto user) {
    // vérifie que le user existe
    final AppUser appUser =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    // vérifie que le pseudo n'est ni null ni vide
    if (user.getPseudo() != null && !user.getPseudo().isBlank()) {
      appUser.setPseudo(user.getPseudo().trim());
    }

    // enregistre la date de mise à jour de l'utilisateur
    appUser.setUpdatedAt(Instant.now());

    // enregistre en base
    userRepository.save(appUser);
  }

  @Override
  public UserDto getCurrentUser() {
    // récupère le contexte de sécurité Spring qui contient les infos d'authent@
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // récupère le principal ici un JWT
    final Jwt jwt = (Jwt) authentication.getPrincipal();

    // récupère le subject du token ici l'email
    final UUID userId = UUID.fromString(jwt.getSubject());

    final AppUser user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    return userMapper.toDto(user);
  }

  @Override
  public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream().map(userMapper::toDto).toList();
  }
}
