package com.ienrique.ressourceRelationnelle.mapper;

import org.mapstruct.Mapper;

import com.ienrique.ressourceRelationnelle.dto.activity.QuizQuestionDto;
import com.ienrique.ressourceRelationnelle.entity.QuizQuestion;

@Mapper(componentModel = "spring")
public interface QuizQuestionMapper {

  QuizQuestionDto toDto(QuizQuestion quizQuestion);
}
