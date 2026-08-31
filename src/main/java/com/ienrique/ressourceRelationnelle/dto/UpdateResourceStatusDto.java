package com.ienrique.ressourceRelationnelle.dto;

import com.ienrique.ressourceRelationnelle.entity.ResourceStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateResourceStatusDto {

  @NotNull private ResourceStatus status;
}
