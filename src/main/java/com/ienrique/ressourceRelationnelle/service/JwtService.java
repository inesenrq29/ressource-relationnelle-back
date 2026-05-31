package com.ienrique.ressourceRelationnelle.service;

import com.ienrique.ressourceRelationnelle.dto.AuthTokenDto;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.entity.RefreshToken;

public interface JwtService {
  String generateAccessToken(AppUser user);

  String generateRefreshToken(AppUser user, String ipAddress, String userAgent);

  void revokeToken(String rawToken);

  RefreshToken validateRefreshToken(String rawToken);

  AuthTokenDto rotateRefreshToken(String rawToken, String ipAddress, String userAgent);
}
