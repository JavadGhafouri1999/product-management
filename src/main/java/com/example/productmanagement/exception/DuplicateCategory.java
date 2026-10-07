package com.example.productmanagement.exception;

public class DuplicateCategory extends RuntimeException {
  public DuplicateCategory(String categoryName) {
	super("The category " + categoryName + " already exist");
  }
}
