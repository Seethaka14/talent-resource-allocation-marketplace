-- ==============================================================================
-- TALENT RESOURCE MARKETPLACE: V2 USERS & ROLE-BASED ACCESS MIGRATION
-- File: src/main/resources/db/migration/V2__create_users_table.sql
-- Description: Creates the users table for authentication and role-based access.
-- Compatibility: MySQL 8.0+, Flyway 10+, Hibernate 6+
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(150) NOT NULL,
  `enabled` bit(1) NOT NULL DEFAULT b'1',
  `name` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('PROJECT_MANAGER','RESOURCE_MANAGER') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
