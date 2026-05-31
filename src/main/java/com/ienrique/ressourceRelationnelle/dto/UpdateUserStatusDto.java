package com.ienrique.ressourceRelationnelle.dto;

import com.ienrique.ressourceRelationnelle.entity.AccountStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserStatusDto {

  @NotNull private AccountStatus status;
}
