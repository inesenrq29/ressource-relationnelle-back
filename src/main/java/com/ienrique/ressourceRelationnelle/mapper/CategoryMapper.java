package com.ienrique.ressourceRelationnelle.mapper;

import org.mapstruct.Mapper;

import com.ienrique.ressourceRelationnelle.dto.CategoryDto;
import com.ienrique.ressourceRelationnelle.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
  CategoryDto toDto(Category category);

  Category toEntity(CategoryDto categoryDto);
}
