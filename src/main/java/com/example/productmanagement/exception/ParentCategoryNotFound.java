package com.example.productmanagement.exception;

public class ParentCategoryNotFound extends RuntimeException {
  public ParentCategoryNotFound(String categoryId) {
	super("The category " + categoryId + " does not exist");
  }
}
