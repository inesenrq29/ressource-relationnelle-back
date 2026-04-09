package com.ienrique.ressourceRelationnelle.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SharedResourceDto {

  private UserInfoDto sender;

  private Instant sharedAt;

  private String message;

  private ResourceDto resource;
}
