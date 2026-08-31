package com.ienrique.ressourceRelationnelle.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenDto {

  private String accessToken;
  private String refreshToken;
  private UserDto userDto;
}
