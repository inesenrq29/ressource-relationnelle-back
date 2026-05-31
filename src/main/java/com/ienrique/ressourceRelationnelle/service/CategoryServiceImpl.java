package com.ienrique.ressourceRelationnelle.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ienrique.ressourceRelationnelle.dto.CategoryDto;
import com.ienrique.ressourceRelationnelle.dto.CreateCategoryDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateCategoryDto;
import com.ienrique.ressourceRelationnelle.entity.Category;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.CategoryMapper;
import com.ienrique.ressourceRelationnelle.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  @Override
  public CategoryDto getCategoryById(UUID categoryId) {
    final Category category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new NotFoundException("Category not found"));
    return categoryMapper.toDto(category);
  }

  @Override
  public List<CategoryDto> getCategories() {
    return categoryRepository.findAll().stream().map(categoryMapper::toDto).toList();
  }

  @Override
  public CategoryDto createCategory(CreateCategoryDto createCategoryDto) {
    final Category category = new Category();
    category.setName(createCategoryDto.getName());

    if (createCategoryDto.getName() == null || createCategoryDto.getName().isBlank()) {
      throw new BadRequestException("Category name is required");
    }

    if (categoryRepository.existsByName(createCategoryDto.getName())) {
      throw new BadRequestException("Category already exists");
    }

    final Category savedCategory = categoryRepository.save(category);
    return categoryMapper.toDto(savedCategory);
  }

  @Override
  @Transactional
  public void updateCategory(UUID categoryId, UpdateCategoryDto updateCategoryDto) {
    final Category category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new NotFoundException("Category not found"));

    if (updateCategoryDto.getName() == null || updateCategoryDto.getName().isBlank()) {
      throw new BadRequestException("Category name is required");
    }

    if (categoryRepository.existsByName(updateCategoryDto.getName())
        && !category.getName().equals(updateCategoryDto.getName())) {
      throw new BadRequestException("Category already exists");
    }

    category.setName(updateCategoryDto.getName());
  }

  @Override
  @Transactional
  public void deleteCategory(UUID categoryId) {
    final Category category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new NotFoundException("Category not found"));

    if (!category.getResources().isEmpty()) {
      throw new BadRequestException("Cannot delete category with associated resources");
    }

    categoryRepository.delete(category);
  }
}
