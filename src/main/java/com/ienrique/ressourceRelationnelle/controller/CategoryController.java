package com.ienrique.ressourceRelationnelle.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ienrique.ressourceRelationnelle.dto.CategoryDto;
import com.ienrique.ressourceRelationnelle.dto.CreateCategoryDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateCategoryDto;
import com.ienrique.ressourceRelationnelle.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping("/{categoryId}")
  public ResponseEntity<CategoryDto> getCategoryById(@PathVariable final UUID categoryId) {
    final CategoryDto response = categoryService.getCategoryById(categoryId);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<CategoryDto>> getCategories() {
    final List<CategoryDto> response = categoryService.getCategories();
    return ResponseEntity.ok(response);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<CategoryDto> createCategory(
      @Valid @RequestBody final CreateCategoryDto request) {
    final CategoryDto response = categoryService.createCategory(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{categoryId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Void> updateCategory(
      @PathVariable final UUID categoryId, @Valid @RequestBody UpdateCategoryDto request) {
    categoryService.updateCategory(categoryId, request);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{categoryId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Void> deleteCategory(@PathVariable final UUID categoryId) {
    categoryService.deleteCategory(categoryId);
    return ResponseEntity.noContent().build();
  }
}
