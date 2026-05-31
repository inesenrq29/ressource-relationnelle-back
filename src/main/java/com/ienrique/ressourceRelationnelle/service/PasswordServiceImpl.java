package com.ienrique.ressourceRelationnelle.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ienrique.ressourceRelationnelle.dto.ChangePasswordDto;
import com.ienrique.ressourceRelationnelle.dto.ForgotPasswordDto;
import com.ienrique.ressourceRelationnelle.dto.ResetPasswordDto;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.entity.PasswordResetToken;
import com.ienrique.ressourceRelationnelle.entity.TokenType;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.repository.AppUserRepository;
import com.ienrique.ressourceRelationnelle.repository.PasswordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordServiceImpl implements PasswordService {

  private final AppUserRepository userRepository;
  private final PasswordRepository passwordRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void requestResetPassword(ForgotPasswordDto forgotPasswordDto) {
    // on vérifie que le mail existe
    final String mail = forgotPasswordDto.getEmail().toLowerCase().trim();
    // on vérifie si le token est bien valide + s'il a le type RESET_PASSWORD
    // s'il est présent on le supprime et on crée un token pour la création du nouveau mot de passe
    userRepository
        .findByMail(mail)
        .ifPresent(
            appUser -> {
              passwordRepository.deleteByUser_AppUserIdAndType(
                  appUser.getAppUserId(), TokenType.RESET_PASSWORD);

              final PasswordResetToken resetToken = new PasswordResetToken();
              resetToken.setUser(appUser);
              resetToken.setUsed(false);
              resetToken.setType(TokenType.RESET_PASSWORD);
              resetToken.setTokenValue(UUID.randomUUID().toString());
              resetToken.setExpiresAt(Instant.now().plusSeconds(900));
              resetToken.setCreatedAt(Instant.now());

              passwordRepository.save(resetToken);
              // ajout temporaire d'un lien pour tester le lien contenant le token
              final String resetLink =
                  "http://localhost:4200/reset-password?token=" + resetToken.getTokenValue();

              System.out.println("Reset password link : " + resetLink);
            });
  }

  @Override
  public void resetPassword(ResetPasswordDto resetPasswordDto) {
    // on vérifie que le token possède le bon type
    final PasswordResetToken resetToken =
        passwordRepository
            .findByTokenValueAndType(resetPasswordDto.getToken(), TokenType.RESET_PASSWORD)
            .orElseThrow(() -> new BadRequestException("Invalid token"));

    // on vérifie que le token n'est pas déjà utilisé
    if (resetToken.isUsed()) {
      throw new BadRequestException("Token has been already used");
    }

    // on vérifie que le token n'est pas expiré
    if (resetToken.getExpiresAt().isBefore(Instant.now())) {
      throw new BadRequestException("Token expired");
    }

    // on rentre le nouveau mot de passe (qui doit respecter une certaine taille etc)
    final AppUser user = resetToken.getUser();

    if (passwordEncoder.matches(resetPasswordDto.getNewPassword(), user.getHashedPassword())) {
      throw new BadRequestException("Passwords must be different");
    }

    user.setHashedPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
    userRepository.save(user);
    // on met le token actuel used à true pour indiquer qu'il a été utilisé

    resetToken.setUsed(true);
    passwordRepository.save(resetToken);
    // on enregistre en BDD

  }

  @Override
  public void changePassword(UUID appUserId, ChangePasswordDto changePasswordDto) {
    final AppUser user =
        userRepository
            .findById(appUserId)
            .orElseThrow(() -> new NotFoundException("User not found"));
    // on vérifie que le mdp actuel est = à celui hash en BDD
    if (!passwordEncoder.matches(
        changePasswordDto.getCurrentPassword(), user.getHashedPassword())) {
      throw new BadRequestException("Invalid current password");
    }
    // on vérifie que le nv mdp hash n'est pas le même que celui en BDD actuel
    if (passwordEncoder.matches(changePasswordDto.getNewPassword(), user.getHashedPassword())) {
      throw new BadRequestException("Passwords must be different");
    }

    // on encode le nouveau mot de passe avant de l'enregistrer

    user.setHashedPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
    userRepository.save(user);
  }
}
