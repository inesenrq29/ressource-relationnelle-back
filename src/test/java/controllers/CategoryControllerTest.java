package controllers;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ienrique.ressourceRelationnelle.RessourceRelationnelleApplication;
import com.ienrique.ressourceRelationnelle.controller.CategoryController;
import com.ienrique.ressourceRelationnelle.dto.CategoryDto;
import com.ienrique.ressourceRelationnelle.dto.CreateCategoryDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateCategoryDto;
import com.ienrique.ressourceRelationnelle.service.CategoryService;

@WebMvcTest(CategoryController.class)
@ContextConfiguration(classes = RessourceRelationnelleApplication.class)
public class CategoryControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CategoryService categoryService;

  @Nested
  @DisplayName("get category by id")
  class GetCategoryById {

    @Test
    @DisplayName("should return category")
    void shouldReturnCategory() throws Exception {
      final UUID categoryId = UUID.randomUUID();
      final CategoryDto categoryDto = new CategoryDto();
      categoryDto.setCategoryId(categoryId);

      when(categoryService.getCategoryById(categoryId)).thenReturn(categoryDto);

      mockMvc
          .perform(
              get("/api/categories/{categoryId}", categoryId)
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("get categories")
  class GetCategories {

    @Test
    @DisplayName("should return categories")
    void shouldReturnCategories() throws Exception {
      final UUID categoryId = UUID.randomUUID();
      final CategoryDto categoryDto = new CategoryDto();
      final CategoryDto categoryDto2 = new CategoryDto();
      categoryDto.setCategoryId(categoryId);

      when(categoryService.getCategories()).thenReturn(List.of(categoryDto, categoryDto2));

      mockMvc
          .perform(get("/api/categories").with(jwt()).contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("create category")
  class CreateCategory {

    @Test
    @DisplayName("should create category")
    void shouldCreateCategory() throws Exception {
      final CreateCategoryDto request = new CreateCategoryDto();
      request.setName("Category name");
      final CategoryDto category = new CategoryDto();

      when(categoryService.createCategory(request)).thenReturn(category);

      mockMvc
          .perform(
              post("/api/categories")
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());
    }
  }

  @Nested
  @DisplayName("update category")
  class UpdateCategory {

    @Test
    @DisplayName("should update category")
    void shouldUpdateCategory() throws Exception {
      final UUID categoryId = UUID.randomUUID();
      final UpdateCategoryDto request = new UpdateCategoryDto();
      request.setName("Updated category");

      categoryService.updateCategory(categoryId, request);

      mockMvc
          .perform(
              put("/api/categories/{categoryId}", categoryId)
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNoContent());
    }
  }

  @Nested
  @DisplayName("delete category")
  class DeleteCategory {

    @Test
    @DisplayName("should delete category")
    void shouldDeleteCategory() throws Exception {
      final UUID categoryId = UUID.randomUUID();

      categoryService.deleteCategory(categoryId);

      mockMvc
          .perform(
              delete("/api/categories/{categoryId}", categoryId)
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNoContent());
    }
  }
}
