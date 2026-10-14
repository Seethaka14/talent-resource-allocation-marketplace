-- ==============================================================================
-- TALENT RESOURCE MARKETPLACE: V1 INITIAL SCHEMA MIGRATION
-- File: src/main/resources/db/migration/V1__initial_schema.sql
-- Description: Baseline schema definition matching JPA entities exactly.
-- Compatibility: MySQL 8.0+, Flyway 10+, Hibernate 6+
-- ==============================================================================

-- 1. EMPLOYEES TABLE
CREATE TABLE IF NOT EXISTS `employees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `department` varchar(50) NOT NULL,
  `email` varchar(150) NOT NULL,
  `name` varchar(100) NOT NULL,
  `employee_code` varchar(30) NOT NULL,
  `employment_status` enum('ACTIVE','INACTIVE','ON_LEAVE') NOT NULL,
  `location` varchar(100) NOT NULL,
  `designation` varchar(50) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `joining_date` date NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `max_capacity_percent` int NOT NULL DEFAULT 100,
  `updated_at` datetime(6) DEFAULT NULL,
  `version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employees_employee_code` (`employee_code`),
  UNIQUE KEY `uk_employees_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. SKILLS TABLE
CREATE TABLE IF NOT EXISTS `skills` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` enum('ARCHITECTURE','BACKEND','CLOUD_DEVOPS','DATABASE','DATA_AI','FRONTEND','MANAGEMENT','MOBILE','SECURITY','TESTING') NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_skills_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. EMPLOYEE SKILLS TABLE
CREATE TABLE IF NOT EXISTS `employee_skills` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `evidence_notes` varchar(1000) DEFAULT NULL,
  `last_validated_at` date DEFAULT NULL,
  `proficiency_level` enum('ADVANCED','BEGINNER','INTERMEDIATE') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `validated_by` varchar(100) DEFAULT NULL,
  `years_of_experience` double DEFAULT 0.0,
  `employee_id` bigint NOT NULL,
  `skill_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_skills_emp_skill` (`employee_id`,`skill_id`),
  KEY `idx_emp_skills_skill` (`skill_id`),
  CONSTRAINT `fk_employee_skills_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`),
  CONSTRAINT `fk_employee_skills_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. EMPLOYEE UNAVAILABILITIES TABLE
CREATE TABLE IF NOT EXISTS `employee_unavailabilities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `end_date` date NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `start_date` date NOT NULL,
  `type` enum('ANNUAL_LEAVE','MATERNITY_PATERNITY','OTHER','PUBLIC_HOLIDAY','SABBATICAL','SICK_LEAVE','TRAINING') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_unavail_emp_dates` (`employee_id`,`start_date`,`end_date`),
  CONSTRAINT `fk_employee_unavailabilities_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 5. PROJECT DEMANDS TABLE
CREATE TABLE IF NOT EXISTS `project_demands` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `allocation_percent` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `demand_code` varchar(30) NOT NULL,
  `department` varchar(50) DEFAULT NULL,
  `end_date` date NOT NULL,
  `location` varchar(100) NOT NULL,
  `priority` enum('CRITICAL','HIGH','LOW','MEDIUM') NOT NULL,
  `project_name` varchar(100) NOT NULL,
  `required_experience_years` double DEFAULT 0.0,
  `role` varchar(50) NOT NULL,
  `start_date` date NOT NULL,
  `status` enum('ALLOCATED','CANCELLED','MATCHED','OPEN') NOT NULL DEFAULT 'OPEN',
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_demands_code` (`demand_code`),
  KEY `idx_demands_status_priority` (`status`,`priority`),
  KEY `idx_demands_dates` (`start_date`,`end_date`),
  CONSTRAINT `chk_demands_allocation_percent` CHECK (((`allocation_percent` >= 1) and (`allocation_percent` <= 100)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 6. DEMAND SKILLS TABLE
CREATE TABLE IF NOT EXISTS `demand_skills` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `mandatory` bit(1) NOT NULL,
  `minimum_proficiency` enum('ADVANCED','BEGINNER','INTERMEDIATE') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `demand_id` bigint NOT NULL,
  `skill_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_demand_skills_demand_skill` (`demand_id`,`skill_id`),
  KEY `idx_demand_skills_skill` (`skill_id`),
  CONSTRAINT `fk_demand_skills_demand` FOREIGN KEY (`demand_id`) REFERENCES `project_demands` (`id`),
  CONSTRAINT `fk_demand_skills_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 7. RESOURCE ALLOCATIONS TABLE
CREATE TABLE IF NOT EXISTS `resource_allocations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `allocated_by` varchar(100) DEFAULT NULL,
  `allocation_percent` int NOT NULL,
  `confirmed_at` datetime(6) DEFAULT NULL,
  `confirmed_by` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `end_date` date NOT NULL,
  `rejection_reason` varchar(500) DEFAULT NULL,
  `start_date` date NOT NULL,
  `status` enum('CANCELLED','COMPLETED','CONFIRMED','PENDING','REJECTED') NOT NULL DEFAULT 'PENDING',
  `updated_at` datetime(6) DEFAULT NULL,
  `version` bigint NOT NULL DEFAULT 0,
  `demand_id` bigint NOT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_alloc_emp_dates_status` (`employee_id`,`start_date`,`end_date`,`status`),
  KEY `idx_alloc_demand` (`demand_id`),
  KEY `idx_alloc_status` (`status`),
  CONSTRAINT `fk_allocations_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`),
  CONSTRAINT `fk_allocations_demand` FOREIGN KEY (`demand_id`) REFERENCES `project_demands` (`id`),
  CONSTRAINT `chk_allocations_allocation_percent` CHECK (((`allocation_percent` >= 1) and (`allocation_percent` <= 100)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
