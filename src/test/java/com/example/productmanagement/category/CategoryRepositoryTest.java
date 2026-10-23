package com.example.productmanagement.category;

import com.example.productmanagement.category.entity.Category;
import com.example.productmanagement.category.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.flyway.enabled=true"
})
@Testcontainers
@DisplayName("Category Repository Integration Tests")
public class CategoryRepositoryTest {

  // 1. THIS IS REQUIRED. It satisfies @EnableCaching so the test context can start.
  @TestConfiguration
  static class TestCacheConfig {
	@Bean
	CacheManager cacheManager() {
	  return new NoOpCacheManager();
	}
  }

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

  @Autowired
  private CategoryRepository categoryRepository;

  private Category createCategory(String name, Category parent) {
	Category category = new Category();
	category.setName(name);
	category.setParent(parent);
	return categoryRepository.saveAndFlush(category);
  }

  //* ── Database Constraint Tests ──────────────────────────────────────────────────────

  /**
   * Test: The UNIQUE constraint blocks duplicate names under the same parent.
   */
  @Test
  @DisplayName("Save fails when name already exists under same parent")
  void save_shouldFail_whenNameAlreadyExistsUnderSameParent() {
	Category parent = createCategory("Electronics", null);
	createCategory("Laptops", parent);

	assertThrows(DataIntegrityViolationException.class,
			() -> createCategory("Laptops", parent));
  }

  /**
   * Test: The same name under a different parent is allowed.
   */
  @Test
  @DisplayName("Save succeeds when same name exists under different parent")
  void save_shouldSucceed_whenSameNameUnderDifferentParent() {
	Category parent1 = createCategory("Electronics", null);
	Category parent2 = createCategory("Clothes", null);
	createCategory("Accessories", parent1);

	assertDoesNotThrow(() -> createCategory("Accessories", parent2));
  }

  /**
   * Test: NULLS NOT DISTINCT blocks two root categories with the same name.
   */
  @Test
  @DisplayName("Save fails when two root categories have the same name")
  void save_shouldFail_whenTwoRootsHaveSameName() {
	createCategory("Electronics", null);

	assertThrows(DataIntegrityViolationException.class,
			() -> createCategory("Electronics", null));
  }

  //* ── Self-exclusion Query Tests ─────────────────────────────────────────────────────

  /**
   * Test: existsByNameAndParentIdAndIdNot returns false
   * <p>
   * when only the category itself matches.
   */
  @Test
  @DisplayName("IdNot query excludes the category itself under a parent")
  void existsByNameAndParentIdAndIdNot_shouldReturnFalse_whenOnlyItselfMatches() {

	Category parent = createCategory("Electronics", null);
	Category laptop = createCategory("Laptops", parent);

	boolean exists = categoryRepository
			.existsByNameAndParentIdAndIdNot("Laptops", parent.getId(), laptop.getId());

	assertFalse(exists);
  }

  /**
   * Test: existsByNameAndParentIdAndIdNot returns true
   * <p>
   * when another category has the same name.
   */
  @Test
  @DisplayName("IdNot query finds another category under the same parent")
  void existsByNameAndParentIdAndIdNot_shouldReturnTrue_whenAnotherCategoryMatches() {
	Category parent = createCategory("Electronics", null);
	createCategory("Laptops", parent);
	Category phone = createCategory("S25-Ultra", parent);

	boolean exists = categoryRepository
			.existsByNameAndParentIdAndIdNot("Laptops", parent.getId(), phone.getId());

	assertTrue(exists);
  }

  /**
   * Test: existsByNameAndParentIsNullAndIdNot returns false
   * <p>
   * when only the root category itself matches.
   */
  @Test
  @DisplayName("IdNot query excludes the root category itself")
  void existsByNameAndParentIsNullAndIdNot_shouldReturnFalse_whenOnlyItselfMatches() {
	Category electronics = createCategory("Electronics", null);

	boolean exists = categoryRepository.existsByNameAndParentIsNullAndIdNot("Electronics", electronics.getId());

	assertFalse(exists);
  }

  /**
   * Scenario: the user renames the root "Computers" to "Electronics".
   * <p>
   * Another root already owns the name "Electronics".
   * <p>
   * The query must find that other root, even though it excludes
   * <p>
   * the id of the category being updated.
   */
  @Test
  @DisplayName("IdNot query returns true when another root owns the requested name")
  void existsByNameAndParentIsNullAndIdNot_shouldReturnTrue_whenAnotherRootMatches() {
	// The existing root that owns the name.
	createCategory("Electronics", null);

	// The category the user is renaming.
	Category computers = createCategory("Computers", null);

	// Simulates the service call: new name = "Electronics",
	// excluded id = the category being updated.
	boolean exists = categoryRepository.existsByNameAndParentIsNullAndIdNot(
			"Electronics", computers.getId());

	assertTrue(exists);
  }

  //* ── Tree Query Tests ───────────────────────────────────────────────────────────────

  /**
   * Test: findAllByParentIsNull returns only root categories.
   */
  @Test
  @DisplayName("findAllByParentIsNull returns only roots")
  void findAllByParentIsNull_shouldReturnOnlyRootCategories() {
	Category root = createCategory("Electronics", null);
	createCategory("Laptops", root);

	List<Category> roots = categoryRepository.findAllByParentIsNull();

	assertEquals(1, roots.size());
	assertEquals(root.getId(), roots.getFirst().getId());
  }

  /**
   * Test: findAllByParentId returns only direct children, not grandchildren.
   */
  @Test
  @DisplayName("findAllByParentId returns only direct children")
  void findAllByParentId_shouldReturnOnlyDirectChildren() {
	Category parent = createCategory("Electronics", null);
	Category child = createCategory("Laptops", parent);
	createCategory("Gaming Laptops", child);

	List<Category> children = categoryRepository.findAllByParentId(parent.getId());

	assertEquals(1, children.size());
	assertEquals(child.getId(), children.getFirst().getId());
  }
}
