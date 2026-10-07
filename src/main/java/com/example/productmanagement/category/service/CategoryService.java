package com.example.productmanagement.category.service;

import com.example.productmanagement.category.dto.CategoryRequestDto;
import com.example.productmanagement.category.dto.CategoryResponseDto;
import com.example.productmanagement.category.entity.Category;
import com.example.productmanagement.category.mapper.CategoryMapper;
import com.example.productmanagement.category.repository.CategoryRepository;
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

  //* ── Create Category ────────────────────────────────────────────────────────────────
  @Transactional
  public CategoryResponseDto createCategory(CategoryRequestDto request) {

	// ── Possible Know Errors ────────────────────────────────────────────────────────────

	// If parent provided in request check if exist
	Category parent = null;
	if (request.parentId() != null) {
	  parent = categoryRepo.findById(request.parentId())
			  .orElseThrow(() -> new ParentCategoryNotFound(request.parentId().toString()));

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

  // ── Get All The Main Categories ─────────────────────────────────────────────────────
  @Transactional(readOnly = true)
  public List<CategoryResponseDto> getRootCategories() {

	List<Category> allCategories = categoryRepo.findAllByParentIsNull();

	return allCategories.stream()
			.map(CategoryMapper::toResponse)
			.toList();
  }

  // ── Get Category By Name And Parent ID ──────────────────────────────────────────────
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
}
