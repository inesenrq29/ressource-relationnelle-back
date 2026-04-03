package com.ienrique.ressourceRelationnelle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ienrique.ressourceRelationnelle.dto.FavoriteDto;
import com.ienrique.ressourceRelationnelle.entity.Favorite;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {
  @Mapping(target = "appUserId", source = "appUser.appUserId")
  @Mapping(target = "resourceId", source = "resource.resourceId")
  FavoriteDto toDto(Favorite favorite);
}
