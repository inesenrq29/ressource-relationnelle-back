package com.ienrique.ressourceRelationnelle.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
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
import com.ienrique.ressourceRelationnelle.entity.Role;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.UserMapper;
import com.ienrique.ressourceRelationnelle.repository.AppUserRepository;
import com.ienrique.ressourceRelationnelle.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final Set<String> ADMIN_CREATED_ROLES =
          Set.of("MODERATOR", "ADMIN", "SUPER_ADMIN");

  private final AppUserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserMapper userMapper;
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
  @Transactional
  public void updateUserStatus(UUID userId, AccountStatus status) {
    if (status == null) {
      throw new BadRequestException("Account status is required");
    }

    // vérifie que le user existe
    final AppUser user =
            userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    // met à jour le statut
    user.setStatus(status);

    // synchronise l'état actif du compte
    user.setAppUserIsActive(status == AccountStatus.ACTIVE);

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

    // vérifie que le pseudo n'est ni null ni vide
    if (createAccount.getPseudo() == null || createAccount.getPseudo().isBlank()) {
      throw new BadRequestException("Pseudo is required");
    }

    // vérifie que le mail n'est ni null ni vide
    if (createAccount.getMail() == null || createAccount.getMail().isBlank()) {
      throw new BadRequestException("Mail is required");
    }

    // vérifie que le password n'est ni null ni vide
    if (createAccount.getPassword() == null || createAccount.getPassword().isBlank()) {
      throw new BadRequestException("Password is required");
    }

    // vérifie que le role n'est pas null
    if (createAccount.getRole() == null || createAccount.getRole().getRoleName() == null) {
      throw new BadRequestException("Role is required");
    }

    final String roleName = createAccount.getRole().getRoleName().trim().toUpperCase();

    if (!ADMIN_CREATED_ROLES.contains(roleName)) {
      throw new BadRequestException("Invalid role for admin account creation");
    }

    final Role role =
            roleRepository
                    .findByRoleName(roleName)
                    .orElseThrow(() -> new NotFoundException("Role not found"));

    final AppUser user = new AppUser();

    // création du pseudo
    user.setPseudo(createAccount.getPseudo().trim());

    // création du statut
    user.setStatus(AccountStatus.ACTIVE);
    user.setAppUserIsActive(true);

    user.setCreatedAt(Instant.now());
    user.setUpdatedAt(Instant.now());
    user.setHashedPassword(passwordEncoder.encode(createAccount.getPassword()));
    user.setMail(createAccount.getMail().trim().toLowerCase());

    // ajout du rôle existant en base
    user.setRole(role);

    // enregistrement en base de données
    final AppUser savedUser = userRepository.save(user);

    return userMapper.toDto(savedUser);
  }

  @Override
  @Transactional
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