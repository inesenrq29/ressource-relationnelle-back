package com.ienrique.ressourceRelationnelle.service;

import java.util.List;
import java.util.UUID;

import com.ienrique.ressourceRelationnelle.dto.CategoryDto;
import com.ienrique.ressourceRelationnelle.dto.CreateCategoryDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateCategoryDto;

public interface CategoryService {

  CategoryDto getCategoryById(UUID categoryId);

  List<CategoryDto> getCategories();

  CategoryDto createCategory(CreateCategoryDto createCategoryDto);

  void updateCategory(UUID categoryId, UpdateCategoryDto updateCategoryDto);

  void deleteCategory(UUID categoryId);
}
