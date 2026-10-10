package com.example.productmanagement.category.service;

import com.example.productmanagement.category.dto.CategoryRequestDto;
import com.example.productmanagement.category.dto.CategoryResponseDto;
import com.example.productmanagement.category.entity.Category;
import com.example.productmanagement.category.mapper.CategoryMapper;
import com.example.productmanagement.category.repository.CategoryRepository;
import com.example.productmanagement.exception.CategoryHasChildren;
import com.example.productmanagement.exception.CategoryNotFound;
import com.example.productmanagement.exception.DuplicateCategory;
import com.example.productmanagement.exception.ParentCategoryNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepo;

  //! ═════════════════════════════ Transactional Services ══════════════════════════════

  //* ── Create Category ────────────────────────────────────────────────────────────────
  @Transactional
  public CategoryResponseDto createCategory(CategoryRequestDto request) {

	// If parent provided in request check if exist
	Category parent = null;
	if (request.parentId() != null) {
	  parent = categoryRepo.findById(request.parentId())
			  .orElseThrow(() -> new ParentCategoryNotFound(request.parentId()));

	}
	// Check if the provided name is already exist
	boolean nameAlreadyExist;
	if (parent == null) {
	  nameAlreadyExist = categoryRepo.existsByNameAndParentIsNull(request.name());
	} else {
	  nameAlreadyExist = categoryRepo.existsByNameAndParentId(request.name(), request.parentId());
	}

	if (nameAlreadyExist) {
	  throw new DuplicateCategory(request.name());
	}

	// Map DTO to entity
	Category category = new Category();
	category.setName(request.name());
	category.setParent(parent);

	// Save to database
	Category savedCategory = categoryRepo.save(category);

	return CategoryMapper.toResponse(savedCategory);
  }

  //* ── Update Category ────────────────────────────────────────────────────────────────
  @Transactional
  public CategoryResponseDto updateCategory(UUID categoryId, CategoryRequestDto request) {
	// Search for the category
	Category category = categoryRepo.findById(categoryId)
			.orElseThrow(() -> CategoryNotFound.byId(categoryId));

	// Check for duplicate names, excluding the category itself
	boolean nameAlreadyExists;
	if (request.parentId() == null) {
	  nameAlreadyExists = categoryRepo.existsByNameAndParentIsNullAndIdNot(request.name(), categoryId);
	} else {
	  nameAlreadyExists = categoryRepo.existsByNameAndParentIdAndIdNot(request.name(), request.parentId(), categoryId);
	}

	if (nameAlreadyExists) {
	  throw new DuplicateCategory(request.name());
	}

	// Update the Parent
	if (request.parentId() != null) {
	  // Prevent circular hierarchy
	  if (request.parentId().equals(categoryId)) {
		throw new IllegalArgumentException("A category can not be its own parent");
	  }

	  // Optimization - Check if parent actually changing
	  UUID currentParentUUID = category.getParent() != null ? category.getParent().getId() : null;

	  if (!request.parentId().equals(currentParentUUID)) {

		Category newParent = categoryRepo.findById(request.parentId())
				.orElseThrow(() -> new ParentCategoryNotFound(request.parentId()));

		//! Prevent circular hierarchy
		validateParentChange(category, newParent);

		category.setParent(newParent);
	  }
	} else {
	  // Moving to root
	  category.setParent(null);
	}

	// Update the Name
	category.setName(request.name());

	// Save the updated category
	Category savedCategory = categoryRepo.save(category);
	return CategoryMapper.toResponse(savedCategory);
  }

  //* ── Delete Category ────────────────────────────────────────────────────────────────
  @Transactional
  public void deleteCategory(UUID categoryId) {

	// Search for the category
	Category foundCategory = categoryRepo.findById(categoryId)
			.orElseThrow(() -> CategoryNotFound.byId(categoryId));

	// Check if it has sub-categories - if yes prevent the action
	if (categoryRepo.existsByParentId(categoryId)) {
	  throw new CategoryHasChildren(categoryId);
	}

	// Note: If this category is linked to products, the database's ON DELETE RESTRICT
	// constraint will block this deletion and throw DataIntegrityViolationException.
	// Ensure your GlobalExceptionHandler catches that and returns a 409 Conflict.
	categoryRepo.delete(foundCategory);
  }

  //! ═══════════════════════════════ Read-only Services ════════════════════════════════

  //* ── Get Category By UUID ───────────────────────────────────────────────────────────
  @Transactional(readOnly = true)
  public CategoryResponseDto getCategoryById(UUID categoryUUID) {

	Category foundCategoryById = categoryRepo.findById(categoryUUID)
			.orElseThrow(() -> CategoryNotFound.byId(categoryUUID));

	return CategoryMapper.toResponse(foundCategoryById);
  }

  //* ── Get Child Categories With Parent ID ────────────────────────────────────────────
  @Transactional(readOnly = true)
  public List<CategoryResponseDto> getChildrenByParentId(UUID parentUUID) {

	// simplified - If it doesn't exist just return empty list instead of error
	List<Category> children = categoryRepo.findAllByParentId(parentUUID);

	return children.stream()
			.map(CategoryMapper::toResponse)
			.toList();
  }

  //* ── Get All The Main Categories ────────────────────────────────────────────────────
  @Transactional(readOnly = true)
  public List<CategoryResponseDto> getRootCategories() {

	List<Category> allCategories = categoryRepo.findAllByParentIsNull();

	return allCategories.stream()
			.map(CategoryMapper::toResponse)
			.toList();
  }

  //* ── Get Category By Name And Parent ID ─────────────────────────────────────────────
  // This is For when the frontend explicitly needs exact lookups by name + parent
  @Transactional(readOnly = true)
  public CategoryResponseDto getCategoryByNameAndParent(String name, UUID parentId) {
	Category foundCategory;

	if (parentId == null) {
	  foundCategory = categoryRepo.findByNameAndParentIsNull(name)
			  .orElseThrow(() -> CategoryNotFound.byName(name));
	} else {
	  foundCategory = categoryRepo.findByNameAndParentId(name, parentId)
			  .orElseThrow(() -> CategoryNotFound.byName(name));
	}

	return CategoryMapper.toResponse(foundCategory);
  }

  //! ═════════════════════════════════════ Helper ══════════════════════════════════════
  private void validateParentChange(Category category, Category newParent) {
	Category current = newParent;

	while (current != null) {
	  if (current.getId().equals(category.getId())) {
		throw new IllegalArgumentException("A category cannot be moved under one of its descendants.");
	  }
	  current = current.getParent();
	}
  }
}
