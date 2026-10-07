package com.example.productmanagement.exception;

import java.util.UUID;

public class CategoryNotFound extends RuntimeException {

  private CategoryNotFound(String message) {
	super(message);
  }

  public static CategoryNotFound byId(UUID categoryId) {
	return new CategoryNotFound("Category with ID: " + categoryId + " not found.");
  }

  public static CategoryNotFound byName(String name) {
	return new CategoryNotFound("Category with name: " + name + " not found.");
  }
}
