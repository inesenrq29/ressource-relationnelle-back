package com.ienrique.ressourceRelationnelle.mapper;

import org.mapstruct.Mapper;

import com.ienrique.ressourceRelationnelle.dto.RoleDto;
import com.ienrique.ressourceRelationnelle.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
  RoleDto toDto(Role role);

  Role toEntity(RoleDto roleDto);
}
