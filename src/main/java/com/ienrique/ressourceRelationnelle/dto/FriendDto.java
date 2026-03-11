package com.ienrique.ressourceRelationnelle.dto;

import java.util.UUID;

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

  @NotNull private UUID friendReceiverUserId;
}
