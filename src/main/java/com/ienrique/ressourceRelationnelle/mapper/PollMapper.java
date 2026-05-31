package com.ienrique.ressourceRelationnelle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ienrique.ressourceRelationnelle.dto.activity.PollDto;
import com.ienrique.ressourceRelationnelle.entity.Poll;

@Mapper(componentModel = "spring")
public interface PollMapper {
  @Mapping(target = "resourceId", source = "interactiveResource.resource.resourceId")
  PollDto toDto(Poll poll);
}
