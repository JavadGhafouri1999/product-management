package com.example.productmanagement.category.mapper;

import com.example.productmanagement.category.dto.CategoryResponseDto;
import com.example.productmanagement.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

  public static CategoryResponseDto toResponse(Category category) {

	if (category == null) {
	  return null;
	}

	return new CategoryResponseDto(
			category.getId(),
			category.getName(),
			category.getParent() != null ? category.getParent().getId() : null,
			category.getChildren(),
			category.getCreatedAt(),
			category.getUpdatedAt()
	);
  }
}
