package com.example.productmanagement.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
	body.put("time-stamp", LocalDateTime.now());
	return body;
  }

  @ExceptionHandler(DuplicateCategory.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateCategory(DuplicateCategory ex) {
	log.warn("Category already exist: {}", ex.getMessage());

	return ResponseEntity.status(HttpStatus.CONFLICT).body(buildResponse(409, ex.getMessage()));
  }

  @ExceptionHandler(ParentCategoryNotFound.class)
  public ResponseEntity<Map<String, Object>> handleParentCategoryNotFound(ParentCategoryNotFound ex) {
	log.warn("Category does not exist: {}", ex.getMessage());

	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildResponse(404, ex.getMessage()));
  }

  @ExceptionHandler(CategoryNotFound.class)
  public ResponseEntity<Map<String, Object>> handleCategoryNotFound(CategoryNotFound ex) {
	log.warn("Category not found: {}", ex.getMessage());

	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildResponse(404, ex.getMessage()));
  }

  @ExceptionHandler(CategoryHasChildren.class)
  public ResponseEntity<Map<String, Object>> handleCategoryHasChildren(CategoryHasChildren ex) {
	log.warn("Category has children: {}", ex.getMessage());

	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildResponse(400, ex.getMessage()));
  }


  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
	log.error("Database constraint violation: {}", ex.getMessage());

	// Check if it's your specific unique constraint
	if (ex.getMessage().contains("uq_categories_name_parent")) {
	  return ResponseEntity.status(HttpStatus.CONFLICT)
			  .body(buildResponse(409, "A category with this name already exists under this parent."));
	}

	// Future proofing for when you add Products
	if (ex.getMessage().contains("fk_products_category")) {
	  return ResponseEntity.status(HttpStatus.CONFLICT)
			  .body(buildResponse(409, "Cannot delete category: It is currently in use by products."));
	}

	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(buildResponse(500, "An unexpected database error occurred."));
  }

  // Recommended: Validation Handler for @Valid DTOs
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
	String errorMessage = ex.getBindingResult().getFieldErrors().stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.findFirst()
			.orElse("Validation failed");

	log.warn("Validation failed: {}", errorMessage);
	return ResponseEntity.status(400).body(buildResponse(400, errorMessage));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, Object>> handleMissingParams(MissingServletRequestParameterException ex) {
	String message = "Missing required parameter: '" + ex.getParameterName() + "'";
	return ResponseEntity.status(400).body(buildResponse(400, message));
  }
}
