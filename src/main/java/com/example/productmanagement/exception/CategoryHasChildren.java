package com.example.productmanagement.exception;

import java.util.UUID;

public class CategoryHasChildren extends RuntimeException {
  public CategoryHasChildren(UUID categoryId) {
	super("The category " + categoryId + "has children.");
  }
}
