package com.example.productmanagement.category;

import com.example.productmanagement.category.dto.CategoryResponseDto;
import com.example.productmanagement.category.entity.Category;
import com.example.productmanagement.category.mapper.CategoryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the CategoryMapper.
 * These tests run the real MapStruct generated implementation.
 * They need no Spring context and no mocks.
 * <p>
 * <p>
 * A mapper test must prove four things:
 * <p>
 * Every field maps to the correct DTO field.
 * <p>
 * The nested parent.id maps to parentId.
 * <p>
 * A null parent does not crash the mapper. It produces a null parentId.
 * <p>
 * A null entity returns null instead of throwing an exception.
 */
public class CategoryMapperTest {

  // MapStruct generates CategoryMapperImpl during compilation.
  // This line returns an instance of that generated class.
  private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

  /**
   * Test: All fields map correctly, including the nested parent ID.
   */
  @Test
  @DisplayName("toResponse maps all fields including parent id")
  void toResponse_shouldMapAllFields_whenCategoryHasParent() {
	// Arrange: Build real entities. The mapper only reads getters.
	UUID id = UUID.randomUUID();
	UUID parentId = UUID.randomUUID();
	OffsetDateTime now = OffsetDateTime.now();

	Category parent = new Category();
	parent.setName("Electronics");
	ReflectionTestUtils.setField(parent, "id", parentId);

	Category category = new Category();
	category.setName("Laptops");
	category.setParent(parent);
	ReflectionTestUtils.setField(category, "id", id);
	ReflectionTestUtils.setField(category, "createdAt", now);
	ReflectionTestUtils.setField(category, "updatedAt", now);

	// Act: Map the entity to a DTO.
	CategoryResponseDto dto = categoryMapper.toResponse(category);

	// Assert: Check that the DTO fields are correctly set.
	assertEquals(id, dto.id());
	assertEquals(parentId, dto.parentId());
	assertEquals("Laptops", dto.name());
	assertEquals(now, dto.createdAt());
	assertEquals(now, dto.updatedAt());
  }

  /**
   * Test: A root category has no parent.
   * The mapper must return a null parentId, not an exception.
   */
  @Test
  @DisplayName("toResponse maps null parentId when category is a root")
  void toResponse_shouldMapNullParentId_whenCategoryIsRoot() {

	Category parent = new Category();
	parent.setName("Electronics");
	ReflectionTestUtils.setField(parent, "id", UUID.randomUUID());

	CategoryResponseDto dto = categoryMapper.toResponse(parent);

	assertEquals("Electronics", dto.name());
	assertNull(dto.parentId());
  }

  /**
   * Test: A null entity returns null.
   */
  @Test
  @DisplayName("toResponse returns null when entity is null")
  void toResponse_shouldReturnNull_whenCategoryIsNull() {
	assertNull(categoryMapper.toResponse(null));
  }
}
