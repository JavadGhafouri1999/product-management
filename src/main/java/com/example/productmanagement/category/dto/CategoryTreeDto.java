package com.example.productmanagement.category.dto;

import java.util.Set;
import java.util.UUID;

public record CategoryTreeDto(
		UUID id,
		String name,
		UUID parentId,
		Set<CategoryTreeDto> children) {
}