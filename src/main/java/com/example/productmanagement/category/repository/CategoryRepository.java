package com.example.productmanagement.category.repository;

import com.example.productmanagement.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

  // Parentless categories means they are the main categories in the system - Electronics(main) vs phone(sub)
  List<Category> findAllByParentIsNull();

  List<Category> findAllByParentId(UUID parentId);

  Optional<Category> findByNameAndParentId(String name, UUID parent_id);

  Optional<Category> findByNameAndParentIsNull(String name);

  // Find if a category has children
  boolean existsByParentId(UUID parentId);

  // Find if a category by name - not limited to parent - good for validation
  List<Category> findAllByName(String name);

  // Find a category by name within a specific parent - for checking uniqueness before insert
  boolean existsByNameAndParentId(String name, UUID parentId);

  // Find a category by name that is a root category
  boolean existsByNameAndParentIsNull(String name);
}