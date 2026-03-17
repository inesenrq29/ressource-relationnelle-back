package com.ienrique.ressourceRelationnelle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.entity.AppUser;

@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {
  @Mapping(target = "appUserId", source = "appUserId")
  UserDto toDto(AppUser user);

  AppUser toEntity(UserDto userDto);
}
