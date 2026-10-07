package com.example.productmanagement.category.dto;

import java.util.List;
import java.util.UUID;

public record CategoryDetailsDto(
		UUID id,
		String name,
		List<CategoryResponseDto> parentChain, // Breadcrumbs up to the root
		List<CategoryResponseDto> children     // Immediate children
) {
}
