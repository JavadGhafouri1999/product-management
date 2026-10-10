package com.example.productmanagement.category;

import com.example.productmanagement.category.dto.CategoryRequestDto;
import com.example.productmanagement.category.dto.CategoryResponseDto;
import com.example.productmanagement.category.entity.Category;
import com.example.productmanagement.category.repository.CategoryRepository;
import com.example.productmanagement.category.service.CategoryService;
import com.example.productmanagement.exception.DuplicateCategory;
import com.example.productmanagement.exception.ParentCategoryNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the CategoryService.
 * These tests verify the business logic of the CategoryService in isolation.
 * We use Mockito to mock the CategoryRepository. This prevents the tests from
 * connecting to a real database. It makes the tests run in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Category Service Unit Tests")
public class CategoryServiceTest {

  // The repository is mocked. It does not connect to PostgreSQL.
  @Mock
  private CategoryRepository categoryRepository;

  // The service is injected with the mocked repository.
  @InjectMocks
  private CategoryService categoryService;

  // Test data variables
  private UUID categoryId;
  private UUID parentId;
  private CategoryRequestDto request;

  /**
   * Setup method. Runs before every test.
   * Initializes fresh UUIDs and a standard request object.
   */
  @BeforeEach
  public void setup() {
	categoryId = UUID.randomUUID();
	parentId = UUID.randomUUID();
	request = new CategoryRequestDto("Laptops", parentId);
  }

  //! ── Create Tests ───────────────────────────────────────────────────────────────────

  /**
   * Test: Create fails when the name already exists under the same parent.
   * Expected: DuplicateCategory exception is thrown. Save is never called.
   */
  @Test
  @DisplayName("Create should fail when name already exists under parent")
  void createCategory_shouldThrowException_whenNameAlreadyExistsUnderParent() {
	//* Arrange
	when(categoryRepository.findById(parentId)).thenReturn(Optional.of(new Category()));
	when(categoryRepository.existsByNameAndParentId("Laptops", parentId)).thenReturn(true);

	//* Act & Assert
	assertThrows(DuplicateCategory.class, () -> categoryService.createCategory(request));

	//* Verify
	verify(categoryRepository, never()).save(any(Category.class));
  }

  /**
   * Test: Create fails when the parent ID does not exist.
   * Expected: ParentCategoryNotFound exception is thrown. Save is never called.
   */
  @Test
  @DisplayName("Create should fail when parent is not found")
  void createCategory_ShouldThrowException_WhenParentNotFound() {

	when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

	assertThrows(ParentCategoryNotFound.class, () -> categoryService.createCategory(request));

	verify(categoryRepository, never()).save(any(Category.class));
  }

  /**
   * Test: Create fails when the name already exists under the same parent.
   * Expected: DuplicateCategory exception is thrown. Save is never called.
   */
  @Test
  @DisplayName("Create should succeed when request is valid")
  void createCategory_shouldReturnCategory_whenValidRequest() {
	// Arrange
	Category parent = new Category();
	when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent));
	when(categoryRepository.existsByNameAndParentId("Laptops", parentId)).thenReturn(false);

	// Mock save to return the exact object passed to it.
	when(categoryRepository.save(any(Category.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

	// Act
	CategoryResponseDto response = categoryService.createCategory(request);

	// Assert: Use ArgumentCaptor to inspect the exact entity passed to save().
	ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
	verify(categoryRepository).save(captor.capture());

	// FIXED: Use getValue(), not capture()
	Category savedCategory = captor.getValue();
	assertEquals("Laptops", savedCategory.getName());
	assertEquals(parent, savedCategory.getParent());
	assertNotNull(response);
  }

  //! ── Update Tests ───────────────────────────────────────────────────────────────────

  @Test
  void updateCategory_shouldThrowException_whenNameAlreadyExistsUnderParentOrAsParent() {

	// A simple object is enough. No need to mock it since we don't read its fields before the exception.
	Category category = new Category();

	// Fake DB response to return the category - Optional<Category>
	when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

	// Fake DB response to find duplicate name
	when(categoryRepository.existsByNameAndParentIdAndIdNot("Laptops", parentId, categoryId))
			.thenReturn(true);

	// Execute and assert
	assertThrows(DuplicateCategory.class, () -> categoryService.updateCategory(categoryId, request));

	// Verify it doesn't save it
	verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  @DisplayName("Update should fail when new parent is not found")
  void updateCategory_ShouldThrowException_WhenNewParentNotFound() {
	Category existing = new Category();
	when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existing));
	when(categoryRepository.existsByNameAndParentIdAndIdNot("Laptops", parentId, categoryId))
			.thenReturn(false);
	when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

	assertThrows(ParentCategoryNotFound.class, () -> categoryService.updateCategory(categoryId, request));

	verify(categoryRepository, never()).save(any(Category.class));
  }

  /**
   * Tests the update method when a circular hierarchy is created.
   * Scenario: The user tries to move a category under its own descendant.
   * Expected Result: The service throws IllegalArgumentException.
   * The repository save method is never called.
   */
  @Test
  @DisplayName("Update should fail when creating circular hierarchy")
  void updateCategory_ShouldThrowException_WhenCreatingCircularHierarchy() {

	Category childBeingUpdated = mock(Category.class);
	when(childBeingUpdated.getId()).thenReturn(categoryId);

	// new parent's parent is the child
	Category newParent = mock(Category.class);
	when(newParent.getId()).thenReturn(parentId);
	when(newParent.getParent()).thenReturn(childBeingUpdated);

	when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(childBeingUpdated));

	// Mock the duplicate check to return false.
	when(categoryRepository.existsByNameAndParentIdAndIdNot("Laptops", parentId, categoryId))
			.thenReturn(false);

	// Mock the repository to find the new parent.
	when(categoryRepository.findById(parentId)).thenReturn(Optional.of(newParent));

	assertThrows(IllegalArgumentException.class, () -> categoryService.updateCategory(categoryId, request));
	verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  @DisplayName("Update succeeds when moving category to root")
  void updateCategory_ShouldSucceed_WhenMovingToRoot() {
	Category existingCategory = mock(Category.class);
	when(existingCategory.getId()).thenReturn(categoryId);

	Category currentParent = mock(Category.class);
	when(currentParent.getId()).thenReturn(parentId);
	when(existingCategory.getParent()).thenReturn(currentParent);

	// Create a request with a null parent ID.
	CategoryRequestDto rootRequest = new CategoryRequestDto("Laptops", null);

	// Mock the repository to find the existing category.
	when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

	// Mock the root duplicate check to return false
	when(categoryRepository.existsByNameAndParentIsNullAndIdNot("Laptops", categoryId)).thenReturn(false);

	when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

	categoryService.updateCategory(categoryId, rootRequest);

	// Assert: Verify the parent was set to null.
	verify(existingCategory).setParent(null);

	// Verify: Ensure the save method was called with the updated entity.
	verify(categoryRepository).save(existingCategory);
  }

}
