package com.example.productmanagement.exception;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private Map<String, Object> buildResponse(int status, String message) {
	Map<String, Object> body = new HashMap<>();
	body.put("status", status);
	body.put("message", message);
	body.put("time-stamp", LocalDateTime.now().toLocalDate());
	return body;
  }

  @ExceptionHandler(DuplicateCategory.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateCategory(DuplicateCategory ex) {
	log.warn("Category already exist: {}", ex.getMessage());

	return ResponseEntity.status(HttpStatus.SC_CONFLICT).body(buildResponse(409, ex.getMessage()));
  }

  @ExceptionHandler(ParentCategoryNotFound.class)
  public ResponseEntity<Map<String, Object>> handleParentCategoryNotFound(ParentCategoryNotFound ex) {
	log.warn("Category does not exist: {}", ex.getMessage());

	return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(buildResponse(404, ex.getMessage()));
  }

  @ExceptionHandler(CategoryNotFound.class)
  public ResponseEntity<Map<String, Object>> handleCategoryNotFound(CategoryNotFound ex) {
	log.warn("Category not found: {}", ex.getMessage());

	return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(buildResponse(404, ex.getMessage()));
  }
}
