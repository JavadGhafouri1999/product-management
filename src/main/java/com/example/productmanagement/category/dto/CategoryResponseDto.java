package com.example.productmanagement.category.dto;

import com.example.productmanagement.category.entity.Category;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record CategoryResponseDto(
		UUID id,
		String name,
		UUID parentId,
		Set<Category> children,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}