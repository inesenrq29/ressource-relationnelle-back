package com.ienrique.ressourceRelationnelle.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.entity.AppUser;

@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {
  @Mapping(target = "appUserId", source = "appUserId")
  UserDto toDto(AppUser user);

  @Mapping(target = "status", ignore = true)
  @Mapping(target = "areTermsAccepted", ignore = true)
  @Mapping(target = "privacyPolicyAccepted", ignore = true)
  @Mapping(target = "hashedPassword", ignore = true)
  @Mapping(target = "previousPassword", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  AppUser toEntity(UserDto userDto);
}
