package com.ienrique.ressourceRelationnelle.mapper;

import org.mapstruct.Mapper;

import com.ienrique.ressourceRelationnelle.dto.activity.PollOptionDto;
import com.ienrique.ressourceRelationnelle.entity.PollOption;

@Mapper(componentModel = "spring")
public interface PollOptionMapper {

  PollOptionDto toDto(PollOption pollOption);
}
