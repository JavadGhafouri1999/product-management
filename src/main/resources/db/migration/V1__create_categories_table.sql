-- ============================================================================
-- V1__create_categories_table.sql
--
-- PURPOSE
-- Creates the category hierarchy used by the Product Core module.
--
-- CATEGORY EXAMPLES
-- Foods
-- Drinks
-- Clothes
-- Tools
-- Electronics
--
-- DESIGN
-- Uses the Adjacency List pattern:
--
--     Electronics
--     ├── Computers
--     ├── Phones
--     └── Cameras
--
-- Each category stores the ID of its immediate parent.
-- A NULL parent_id means the category is a root category.

-- ============================================================================
-- TABLE
-- ============================================================================
CREATE TABLE categories
(
    -- Application-level identifier.
    -- UUID prevents predictable sequential identifiers from being exposed.
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    -- Human-readable category name.
    name       VARCHAR(100) NOT NULL,

    -- Self-referencing relationship.
    -- NULL means this is a root category.
    parent_id  UUID REFERENCES categories (id) ON DELETE RESTRICT,

    -- Audit timestamps.
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- INDEXES
-- ============================================================================
-- Used when loading the children of a category.
CREATE INDEX idx_categories_parent_id
    ON categories (parent_id);

-- ============================================================================
-- CONSTRAINTS
-- ============================================================================
-- Prevents two categories with the same name under the same parent.
--
-- Example:
--
-- Electronics
-- ├── Accessories
-- └── Accessories
--
-- The second "Accessories" is not allowed.
ALTER TABLE categories
    ADD CONSTRAINT uq_categories_name_parent
        UNIQUE NULLS NOT DISTINCT (name, parent_id);