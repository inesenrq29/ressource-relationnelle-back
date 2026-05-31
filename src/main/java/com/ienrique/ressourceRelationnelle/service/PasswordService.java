package com.ienrique.ressourceRelationnelle.service;

import java.util.UUID;

import com.ienrique.ressourceRelationnelle.dto.ChangePasswordDto;
import com.ienrique.ressourceRelationnelle.dto.ForgotPasswordDto;
import com.ienrique.ressourceRelationnelle.dto.ResetPasswordDto;

public interface PasswordService {
  void requestResetPassword(ForgotPasswordDto forgotPasswordDto);

  void resetPassword(ResetPasswordDto resetPasswordDto);

  void changePassword(UUID appUserId, ChangePasswordDto changePasswordDto);
}
