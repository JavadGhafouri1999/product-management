package com.example.productmanagement.category.controller;

import com.example.productmanagement.category.dto.CategoryRequestDto;
import com.example.productmanagement.category.dto.CategoryResponseDto;
import com.example.productmanagement.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  //! ════════════════════════════ Main Post - Put - Delete ═════════════════════════════
  // POST /api/v1/categories
  @PostMapping
  public ResponseEntity<CategoryResponseDto> createCategory(
		  @Valid @RequestBody CategoryRequestDto categoryRequest) {

	CategoryResponseDto response = categoryService.createCategory(categoryRequest);

	return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // PUT /api/v1/categories/{id}
  @PutMapping("/{id}")
  public ResponseEntity<CategoryResponseDto> updateCategory(
		  @Valid @RequestBody CategoryRequestDto categoryRequest,
		  @PathVariable UUID id) {

	CategoryResponseDto response = categoryService.updateCategory(id, categoryRequest);

	return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // DELETE /api/v1/categories/{id}
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCategory(
		  @PathVariable UUID id) {
	categoryService.deleteCategory(id);
	return ResponseEntity.noContent().build();
  }

  //! ═══════════════════════════════════ Get Methods ═══════════════════════════════════

  // GET /api/v1/categories/roots
  @GetMapping("/roots")
  public ResponseEntity<List<CategoryResponseDto>> getRootCategories() {
	List<CategoryResponseDto> response = categoryService.getRootCategories();
	return ResponseEntity.ok(response);
  }

  // GET /api/v1/categories/{categoryId}
  @GetMapping("/{categoryId}")
  public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable UUID categoryId) {
	return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
  }

  // GET /api/v1/categories/{parentId}/children
  @GetMapping("/{parentId}/children")
  public ResponseEntity<List<CategoryResponseDto>> getChildren(@PathVariable UUID parentId) {
	return ResponseEntity.ok(categoryService.getChildrenByParentId(parentId));
  }

  @GetMapping("/exact-match")
  public ResponseEntity<CategoryResponseDto> getCategoryByNameAndParent(
		  @RequestParam String name,
		  @RequestParam(required = false) UUID parentId) {

	return ResponseEntity.ok(categoryService.getCategoryByNameAndParent(name, parentId));
  }
}
