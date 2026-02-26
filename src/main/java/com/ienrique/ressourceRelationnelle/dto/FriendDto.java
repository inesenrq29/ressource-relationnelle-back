package com.ienrique.ressourceRelationnelle.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FriendDto {

  @NotNull private UUID friendId;

  @NotNull private UUID requesterUserId;

  @NotNull private UUID receiverUserId;

  @NotBlank private String status;

  @NotNull private Instant createdAt;
}
