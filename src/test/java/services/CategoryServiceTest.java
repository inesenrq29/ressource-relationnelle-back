package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ienrique.ressourceRelationnelle.dto.CategoryDto;
import com.ienrique.ressourceRelationnelle.dto.CreateCategoryDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateCategoryDto;
import com.ienrique.ressourceRelationnelle.entity.Category;
import com.ienrique.ressourceRelationnelle.entity.Resource;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.CategoryMapper;
import com.ienrique.ressourceRelationnelle.repository.CategoryRepository;
import com.ienrique.ressourceRelationnelle.service.CategoryServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;
  @Mock private CategoryMapper categoryMapper;

  @InjectMocks private CategoryServiceImpl categoryService;

  @Nested
  @DisplayName("get category by id")
  class GetCategoryById {

    @Test
    @DisplayName("should return category")
    void shouldReturnCategory() {
      final UUID categoryId = UUID.randomUUID();
      final Category category = new Category();
      category.setCategoryId(categoryId);
      category.setName("Category name");
      final CategoryDto categoryDto = new CategoryDto();

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
      when(categoryMapper.toDto(category)).thenReturn(categoryDto);

      final CategoryDto result = categoryService.getCategoryById(categoryId);

      assertEquals(categoryDto, result);
      verify(categoryMapper).toDto(category);
    }

    @Test
    @DisplayName("should throw category not found")
    void shouldThrowCategoryNotFound() {
      final UUID categoryId = UUID.randomUUID();

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(categoryId));
      verifyNoInteractions(categoryMapper);
    }
  }

  @Nested
  @DisplayName("get categories")
  class GetCategories {

    @Test
    @DisplayName("should return categories")
    void shouldReturnCategories() {
      final Category category1 = new Category();
      final Category category2 = new Category();
      final CategoryDto categoryDto1 = new CategoryDto();
      final CategoryDto categoryDto2 = new CategoryDto();

      when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));
      when(categoryMapper.toDto(category1)).thenReturn(categoryDto1);
      when(categoryMapper.toDto(category2)).thenReturn(categoryDto2);

      final List<CategoryDto> result = categoryService.getCategories();

      assertEquals(2, result.size());
      verify(categoryMapper).toDto(category1);
      verify(categoryMapper).toDto(category2);
    }
  }

  @Nested
  @DisplayName("create category")
  class CreateCategory {

    @Test
    @DisplayName("should create category")
    void shouldCreateCategory() {
      final Category category = new Category();
      category.setName("Category name");
      final CreateCategoryDto createCategoryDto = new CreateCategoryDto();
      createCategoryDto.setName("Category name");
      final CategoryDto categoryDto = new CategoryDto();
      categoryDto.setName("Category name");

      when(categoryRepository.existsByName(createCategoryDto.getName())).thenReturn(false);
      when(categoryRepository.save(any(Category.class))).thenReturn(category);
      when(categoryMapper.toDto(category)).thenReturn(categoryDto);

      final CategoryDto result = categoryService.createCategory(createCategoryDto);
      assertEquals("Category name", result.getName());
      verify(categoryRepository).existsByName("Category name");
      verify(categoryRepository).save(any(Category.class));
      verify(categoryMapper).toDto(category);
    }

    @Test
    @DisplayName("should throw bad request category null")
    void shouldThrowBadRequestCategoryNull() {
      final CreateCategoryDto createCategoryDto = new CreateCategoryDto();
      createCategoryDto.setName(null);

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> categoryService.createCategory(createCategoryDto));

      assertEquals("Category name is required", exception.getMessage());
      verify(categoryRepository, never()).existsByName(any());
      verify(categoryRepository, never()).save(any());
      verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw bad request category blank")
    void shouldThrowBadRequestCategoryBlank() {
      final CreateCategoryDto createCategoryDto = new CreateCategoryDto();
      createCategoryDto.setName("");

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> categoryService.createCategory(createCategoryDto));

      assertEquals("Category name is required", exception.getMessage());
      verify(categoryRepository, never()).existsByName(any());
      verify(categoryRepository, never()).save(any());
      verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw bad request category already exists")
    void shouldThrowBadRequestCategoryExists() {
      final CreateCategoryDto createCategoryDto = new CreateCategoryDto();
      createCategoryDto.setName("Category name");

      when(categoryRepository.existsByName(createCategoryDto.getName())).thenReturn(true);

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> categoryService.createCategory(createCategoryDto));

      assertEquals("Category already exists", exception.getMessage());
      verify(categoryMapper, never()).toDto(any());
    }
  }

  @Nested
  @DisplayName("update category")
  class UpdateCategory {

    @Test
    @DisplayName("should update category")
    void shouldUpdateCategory() {
      final UUID categoryId = UUID.randomUUID();
      final Category category = new Category();
      category.setCategoryId(categoryId);
      category.setName("Current name");
      final UpdateCategoryDto updateCategoryDto = new UpdateCategoryDto();
      updateCategoryDto.setName("Updated name");

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
      when(categoryRepository.existsByName(updateCategoryDto.getName())).thenReturn(false);

      categoryService.updateCategory(categoryId, updateCategoryDto);
      assertEquals("Updated name", category.getName());
      verify(categoryRepository).findById(categoryId);
      verify(categoryRepository).existsByName("Updated name");
    }

    @Test
    @DisplayName("should throw category not found")
    void shouldThrowCategoryNotFound() {
      final UUID categoryId = UUID.randomUUID();

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class,
              () -> categoryService.updateCategory(categoryId, any(UpdateCategoryDto.class)));

      assertEquals("Category not found", exception.getMessage());
      verify(categoryRepository, never()).existsByName(any());
      verify(categoryRepository, never()).save(any());
      verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw bad request category already exists")
    void shouldThrowBadRequestCategoryExists() {
      final UUID categoryId = UUID.randomUUID();
      final UpdateCategoryDto updateCategoryDto = new UpdateCategoryDto();
      updateCategoryDto.setName("Category");
      final Category category = new Category();
      category.setCategoryId(categoryId);
      category.setName("Current name");

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
      when(categoryRepository.existsByName(updateCategoryDto.getName())).thenReturn(true);

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> categoryService.updateCategory(categoryId, updateCategoryDto));

      assertEquals("Category already exists", exception.getMessage());
      verify(categoryRepository, never()).save(any());
      verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw bad request name blank")
    void shouldThrowBadRequestNameBlank() {
      final UUID categoryId = UUID.randomUUID();
      final UpdateCategoryDto updateCategoryDto = new UpdateCategoryDto();
      updateCategoryDto.setName("");
      final Category category = new Category();
      category.setCategoryId(categoryId);
      category.setName("Current name");

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> categoryService.updateCategory(categoryId, updateCategoryDto));

      assertEquals("Category name is required", exception.getMessage());
      verify(categoryRepository, never()).save(any());
      verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw bad request name null")
    void shouldThrowBadRequestNameNull() {
      final UUID categoryId = UUID.randomUUID();
      final UpdateCategoryDto updateCategoryDto = new UpdateCategoryDto();
      updateCategoryDto.setName(null);
      final Category category = new Category();
      category.setCategoryId(categoryId);
      category.setName("Current name");

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> categoryService.updateCategory(categoryId, updateCategoryDto));

      assertEquals("Category name is required", exception.getMessage());
      verify(categoryRepository, never()).save(any());
      verify(categoryMapper, never()).toDto(any());
    }
  }

  @Nested
  @DisplayName("delete category")
  class DeleteCategory {

    @Test
    @DisplayName("should delete category")
    void shouldDeleteCategory() {
      final UUID categoryId = UUID.randomUUID();
      final Category category = new Category();
      category.setCategoryId(categoryId);

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

      categoryService.deleteCategory(categoryId);
    }

    @Test
    @DisplayName("should throw category not found")
    void shouldThrowCategoryNotFound() {
      final UUID categoryId = UUID.randomUUID();
      final Category category = new Category();
      category.setCategoryId(categoryId);

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
      assertThrows(NotFoundException.class, () -> categoryService.deleteCategory(categoryId));
    }

    @Test
    @DisplayName("should throw bad request resource exists")
    void shouldThrowBadRequestResourceExists() {
      final UUID categoryId = UUID.randomUUID();
      final Category category = new Category();
      category.setCategoryId(categoryId);
      final Resource resource = new Resource();
      category.setResources(Set.of(resource));

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
      final BadRequestException exception =
          assertThrows(BadRequestException.class, () -> categoryService.deleteCategory(categoryId));

      assertEquals("Cannot delete category with associated resources", exception.getMessage());
    }
  }
}
