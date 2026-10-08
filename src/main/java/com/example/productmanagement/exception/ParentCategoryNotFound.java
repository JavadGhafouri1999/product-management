package com.example.productmanagement.exception;

import java.util.UUID;

public class ParentCategoryNotFound extends RuntimeException {
  public ParentCategoryNotFound(UUID categoryId) {
	super("The category " + categoryId + " does not exist");
  }
}
