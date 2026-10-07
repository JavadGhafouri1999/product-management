package com.example.productmanagement.category.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoryRequestDto(
		@NotBlank(message = "Category name can not be blank")
		@Size(message = "Category name must be between 3-100 characters", min = 3, max = 100)
		String name,

		@Nullable
		UUID parentId) {
}