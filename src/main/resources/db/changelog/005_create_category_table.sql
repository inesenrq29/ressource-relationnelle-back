-- liquibase formatted sql
-- changeset ines:005_create_category_table

CREATE TABLE Category (
      categoryId CHAR(36) NOT NULL DEFAULT (UUID()),
      name VARCHAR(100) NOT NULL,
      PRIMARY KEY (categoryId),
      UNIQUE KEY uq_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;