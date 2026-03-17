package com.ienrique.ressourceRelationnelle.service;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ienrique.ressourceRelationnelle.dto.AuthTokenDto;
import com.ienrique.ressourceRelationnelle.dto.LoginDto;
import com.ienrique.ressourceRelationnelle.dto.RegisterUserDto;
import com.ienrique.ressourceRelationnelle.entity.AccountStatus;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.entity.Role;
import com.ienrique.ressourceRelationnelle.mapper.UserMapper;
import com.ienrique.ressourceRelationnelle.repository.AppUserRepository;
import com.ienrique.ressourceRelationnelle.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AppUserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final JwtService jwtService;

  @Override
  @Transactional
  public AuthTokenDto signUp(
      final RegisterUserDto request, final String ipAddress, final String userAgent) {

    // on récup l'email
    final String email = request.getEmail().trim().toLowerCase();

    // on retourne une erreur si email existe déjà
    if (userRepository.existsByMail(email)) {
      throw new RuntimeException("Email already exists");
      // TODO: créer exceptions adaptées
    }

    // on vérifie si les deux mdp sont les mêmes
    if (!request.getPassword().equals(request.getConfirmPassword())) {
      throw new RuntimeException("Passwords must be equals");
    }

    // on vérifie si les termes sont acceptés
    if (!request.isAreTermsAccepted()) {
      throw new RuntimeException("Terms must be accepted");
    }

    // on vérifie si la privacy policy est acceptée aussi
    if (!request.isPrivacyPolicyAccepted()) {
      throw new RuntimeException("Privacy Policy must be accepted");
    }

    // on vérifie que le pseudo n'existe pas déjà
    if (userRepository.existsByPseudo(request.getPseudo().trim())) {
      throw new RuntimeException("Pseudo already exists");
    }

    // récupération du role USER
    final Role roleUser =
        roleRepository
            .findByRoleName("USER")
            .orElseThrow(() -> new RuntimeException("USER role not found"));

    // création du user
    final AppUser user = new AppUser();
    user.setMail(email);
    user.setAppUserIsActive(true);
    user.setRole(roleUser);
    user.setPseudo(request.getPseudo());
    user.setStatus(AccountStatus.ACTIVE);
    user.setHashedPassword(passwordEncoder.encode(request.getPassword()));
    user.setAreTermsAccepted(true);
    user.setPrivacyPolicyAccepted(true);

    user.setCreatedAt(Instant.now());
    user.setUpdatedAt(Instant.now());

    // on enregistre en base le user
    final AppUser savedUser = userRepository.save(user);

    // on génère un access token
    final String accessToken = jwtService.generateAccessToken(savedUser);

    // on génère un refresh token
    final String refreshToken = jwtService.generateRefreshToken(savedUser, ipAddress, userAgent);

    return new AuthTokenDto(accessToken, refreshToken, userMapper.toDto(savedUser));
  }

  @Override
  @Transactional
  public AuthTokenDto login(
      final LoginDto requestDto, final String ipAddress, final String userAgent) {

    // on récupère l'email
    final String email = requestDto.getEmail().trim().toLowerCase();

    // on trouve l'utilisateur grâce à son email, si l'email n'est pas connu on renvoie une erreur
    final AppUser user =
        userRepository.findByMail(email).orElseThrow(() -> new RuntimeException("Email not found"));

    // on retourne une erreur si le compte est désactivé
    if (user.getStatus() == AccountStatus.DISABLED) {
      throw new RuntimeException("Account disabled");
    }

    // on vérifie si le mot de passe entré hashé est le même que celui en base
    final boolean hashedPassword =
        passwordEncoder.matches(requestDto.getPassword(), user.getHashedPassword());

    // on retourne une erreur si ce n'est pas le même
    if (!hashedPassword) {
      throw new RuntimeException("Invalid password");
    }

    // on met à jour la dernière connexion au compte
    user.setLastConnectionAt(Instant.now());
    user.setUpdatedAt(Instant.now());

    // on enregistre l'utilisateur en base
    final AppUser savedUser = userRepository.save(user);

    // on génère un access token
    final String accessToken = jwtService.generateAccessToken(savedUser);

    // on génère un refresh token
    final String refreshToken = jwtService.generateRefreshToken(savedUser, ipAddress, userAgent);

    return new AuthTokenDto(accessToken, refreshToken, userMapper.toDto(savedUser));
  }

  @Override
  @Transactional // permet de ne pas ajouter repository.save
  public void logout(final String refreshToken) {
    // on vérifie que le token n'est ni null ni vide
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new RuntimeException("Refresh token is required");
    }

    // on révoque le token
    jwtService.revokeToken(refreshToken);
  }

  @Override
  public AuthTokenDto refreshToken(
      final String refreshToken, final String ipAddress, final String userAgent) {

    // on vérifie que le token n'est ni null ni vide
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new RuntimeException("Refresh token is required");
    }

    // on révoque les tokens actuels pour en générer des nouveaux (= rotation de token)
    return jwtService.rotateRefreshToken(refreshToken, ipAddress, userAgent);
  }
}
