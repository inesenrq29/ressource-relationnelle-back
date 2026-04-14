package com.ienrique.ressourceRelationnelle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ienrique.ressourceRelationnelle.dto.activity.QuizDto;
import com.ienrique.ressourceRelationnelle.entity.Quiz;

@Mapper(componentModel = "spring")
public interface QuizMapper {

  @Mapping(target = "resourceId", source = "interactiveResource.resource.resourceId")
  QuizDto toDto(Quiz quiz);
}
