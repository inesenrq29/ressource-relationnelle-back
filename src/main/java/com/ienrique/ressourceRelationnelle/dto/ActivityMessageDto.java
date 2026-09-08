package com.ienrique.ressourceRelationnelle.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityMessageDto {

  private UUID activityMessageId;
  private UUID senderId;;
  private String SenderUsername;
  private String content;
  private Instant sentAt;
}
