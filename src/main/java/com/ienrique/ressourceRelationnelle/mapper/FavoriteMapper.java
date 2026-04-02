package com.ienrique.ressourceRelationnelle.mapper;

import org.mapstruct.Mapper;

import com.ienrique.ressourceRelationnelle.dto.FavoriteDto;
import com.ienrique.ressourceRelationnelle.entity.Favorite;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {
  FavoriteDto toDto(Favorite favorite);
}
