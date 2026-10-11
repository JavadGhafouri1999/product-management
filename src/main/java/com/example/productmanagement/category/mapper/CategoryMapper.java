package com.example.productmanagement.category.mapper;

import com.example.productmanagement.category.dto.CategoryResponseDto;
import com.example.productmanagement.category.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  @Mapping(source = "parent.id", target = "parentId")
  CategoryResponseDto toResponse(Category category);

  List<CategoryResponseDto> toResponseList(List<Category> categories);
}