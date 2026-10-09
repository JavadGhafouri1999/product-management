package com.example.productmanagement.category;

import com.example.productmanagement.category.dto.CategoryRequestDto;
import com.example.productmanagement.category.entity.Category;
import com.example.productmanagement.category.repository.CategoryRepository;
import com.example.productmanagement.category.service.CategoryService;
import com.example.productmanagement.exception.CategoryNotFound;
import com.example.productmanagement.exception.DuplicateCategory;
import com.example.productmanagement.exception.ParentCategoryNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategoryService categoryService;

  private UUID parentId;
  private UUID childId;
  private UUID grandchildId;
  private UUID categoryId;
  private CategoryRequestDto request;


  @BeforeEach
  public void setup() {
	categoryId = UUID.randomUUID();
	parentId = UUID.randomUUID();
	childId = UUID.randomUUID();
	grandchildId = UUID.randomUUID();
	request = new CategoryRequestDto("Laptops", parentId);
  }

  //! ── Create Tests ───────────────────────────────────────────────────────────────────

  @Test
  void createCategory_shouldThrowException_whenNameAlreadyExistsUnderParent() {
	//* Arrange
	when(categoryRepository.findById(parentId)).thenReturn(Optional.of(new Category()));
	when(categoryRepository.existsByNameAndParentId("Laptops", parentId)).thenReturn(true);

	//* Act & Assert
	assertThrows(DuplicateCategory.class, () -> categoryService.createCategory(request));

	//* Verify
	verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  void createCategory_ShouldThrowException_WhenParentNotFound() {

	when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

	assertThrows(ParentCategoryNotFound.class, () -> categoryService.createCategory(request));

	verify(categoryRepository, never()).save(any(Category.class));
  }

  //! ── Update Tests ───────────────────────────────────────────────────────────────────

  @Test
  void updateCategory_shouldThrowException_whenNameAlreadyExistsUnderParentOrAsParent() {

	// A simple object is enough. No need to mock it since we don't read its fields before the exception.
	Category category = new Category();

	// Fake DB response to return the category - Optional<Category>
	when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

	// Fake DB response to find duplicate name
	when(categoryRepository.existsByNameAndParentId("Laptops", parentId))
			.thenReturn(true);

	// Execute and assert
	assertThrows(DuplicateCategory.class, () -> categoryService.updateCategory(categoryId, request));

	// Verify it doesn't save it
	verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  void updateCategory_ShouldThrowException_WhenCategoryNotFound() {
	// No need to mock anything just a DB response is good enough
	when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

	assertThrows(CategoryNotFound.class, () -> categoryService.updateCategory(categoryId, request));
	verify(categoryRepository, never()).save(any(Category.class));
  }

}
