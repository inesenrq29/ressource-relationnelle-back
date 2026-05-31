package com.ienrique.ressourceRelationnelle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageDto {
  @NotBlank private String content;
}
