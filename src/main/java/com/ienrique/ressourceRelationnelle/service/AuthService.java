package com.ienrique.ressourceRelationnelle.service;

import com.ienrique.ressourceRelationnelle.dto.AuthTokenDto;
import com.ienrique.ressourceRelationnelle.dto.LoginDto;
import com.ienrique.ressourceRelationnelle.dto.RegisterUserDto;

public interface AuthService {
  AuthTokenDto signUp(RegisterUserDto signUpRequestDto, String ipAddress, String userAgent);

  AuthTokenDto login(LoginDto loginRequestDto, String ipAddress, String userAgent);

  void logout(String refreshToken);

  AuthTokenDto refreshToken(String refreshToken, String ipAddress, String userAgent);
}
