-- ==============================================================================
-- TALENT RESOURCE MARKETPLACE: SYNTHETIC DEMO DATASET
-- File: src/main/resources/db/seed/demo_data.sql
-- Description: Idempotent SQL script to seed synthetic demo data.
-- Safe for repeated executions. Preserves existing records without duplicates.
-- ==============================================================================

USE talent_marketplace;

-- ------------------------------------------------------------------------------
-- 1. SEED SKILLS (13 new skills + preserves existing skills)
-- Categories: ARCHITECTURE, BACKEND, CLOUD_DEVOPS, DATABASE, DATA_AI, FRONTEND,
--             MANAGEMENT, MOBILE, SECURITY, TESTING
-- ------------------------------------------------------------------------------

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'Java', 'BACKEND', 'Core Java and modern Enterprise backend development (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'Java');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'Spring Boot', 'BACKEND', 'Spring Boot microservices framework and ecosystem (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'Spring Boot');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'Python', 'BACKEND', 'Python backend and data processing scripting (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'Python');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'TypeScript', 'FRONTEND', 'Typed superset of JavaScript for scalable web apps (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'TypeScript');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'AWS', 'CLOUD_DEVOPS', 'Amazon Web Services cloud architecture and services (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'AWS');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'Docker', 'CLOUD_DEVOPS', 'Containerization platform and container image packaging (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'Docker');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'Kubernetes', 'CLOUD_DEVOPS', 'Container orchestration, clusters, and pod scaling (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'Kubernetes');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'SQL/PostgreSQL', 'DATABASE', 'Relational database schema design and optimized queries (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'SQL/PostgreSQL');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'Kafka', 'BACKEND', 'Distributed event streaming and message broker platform (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'Kafka');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'Cybersecurity', 'SECURITY', 'Security threat modeling, auth hardening, and vulnerability mitigation (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'Cybersecurity');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'System Architecture', 'ARCHITECTURE', 'High-level system decomposition and distributed systems design (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'System Architecture');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'Terraform', 'CLOUD_DEVOPS', 'Infrastructure as Code (IaC) provisioning and automation (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'Terraform');

INSERT INTO skills (name, category, description, created_at, updated_at)
SELECT 'GraphQL', 'BACKEND', 'Declarative data query language for APIs (Demo)', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'GraphQL');


-- ------------------------------------------------------------------------------
-- 2. SEED EMPLOYEES (10 Synthetic Demo Employees with varied profiles)
-- ------------------------------------------------------------------------------

-- DEMO-EMP-001: Prime qualified candidate for DEMO-DEM-001 (Java + AWS Advanced, 100% capacity)
INSERT INTO employees (employee_code, first_name, last_name, name, email, department, designation, location, joining_date, employment_status, max_capacity_percent, version, created_at, updated_at)
SELECT 'DEMO-EMP-001', 'Aria', 'Stark', 'Aria Stark', 'aria.stark.demo@talentmarketplace.com', 'Engineering', 'Lead Backend Architect', 'New York', '2021-03-15', 'ACTIVE', 100, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'DEMO-EMP-001');

-- DEMO-EMP-002: Lacks mandatory AWS skill (has Java, Spring Boot, Docker)
INSERT INTO employees (employee_code, first_name, last_name, name, email, department, designation, location, joining_date, employment_status, max_capacity_percent, version, created_at, updated_at)
SELECT 'DEMO-EMP-002', 'Marcus', 'Brody', 'Marcus Brody', 'marcus.brody.demo@talentmarketplace.com', 'Engineering', 'Senior Backend Engineer', 'New York', '2022-06-01', 'ACTIVE', 100, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'DEMO-EMP-002');

-- DEMO-EMP-003: Insufficient proficiency (Java BEGINNER, required INTERMEDIATE)
INSERT INTO employees (employee_code, first_name, last_name, name, email, department, designation, location, joining_date, employment_status, max_capacity_percent, version, created_at, updated_at)
SELECT 'DEMO-EMP-003', 'Elena', 'Rostova', 'Elena Rostova', 'elena.rostova.demo@talentmarketplace.com', 'Engineering', 'Junior Java Developer', 'New York', '2024-01-15', 'ACTIVE', 100, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'DEMO-EMP-003');

-- DEMO-EMP-004: Fully skilled but has overlapping annual leave during demand period
INSERT INTO employees (employee_code, first_name, last_name, name, email, department, designation, location, joining_date, employment_status, max_capacity_percent, version, created_at, updated_at)
SELECT 'DEMO-EMP-004', 'Devon', 'Vance', 'Devon Vance', 'devon.vance.demo@talentmarketplace.com', 'Engineering', 'Staff Cloud Engineer', 'New York', '2020-09-10', 'ACTIVE', 100, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'DEMO-EMP-004');

-- DEMO-EMP-005: Fully skilled but insufficient remaining capacity (max 30%, demand needs 50%)
INSERT INTO employees (employee_code, first_name, last_name, name, email, department, designation, location, joining_date, employment_status, max_capacity_percent, version, created_at, updated_at)
SELECT 'DEMO-EMP-005', 'Kavita', 'Sharma', 'Kavita Sharma', 'kavita.sharma.demo@talentmarketplace.com', 'Engineering', 'Principal Consultant', 'New York', '2019-11-01', 'ACTIVE', 30, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'DEMO-EMP-005');

-- DEMO-EMP-006: Frontend specialist (React, TypeScript, GraphQL)
INSERT INTO employees (employee_code, first_name, last_name, name, email, department, designation, location, joining_date, employment_status, max_capacity_percent, version, created_at, updated_at)
SELECT 'DEMO-EMP-006', 'Liam', 'Gallagher', 'Liam Gallagher', 'liam.gallagher.demo@talentmarketplace.com', 'Frontend & Mobile', 'Senior UI Engineer', 'Remote', '2022-04-12', 'ACTIVE', 100, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'DEMO-EMP-006');

-- DEMO-EMP-007: Data specialist (Python, PostgreSQL, Kafka)
INSERT INTO employees (employee_code, first_name, last_name, name, email, department, designation, location, joining_date, employment_status, max_capacity_percent, version, created_at, updated_at)
SELECT 'DEMO-EMP-007', 'Sofia', 'Chen', 'Sofia Chen', 'sofia.chen.demo@talentmarketplace.com', 'Data Engineering', 'Data Platform Engineer', 'San Francisco', '2021-08-20', 'ACTIVE', 80, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'DEMO-EMP-007');

-- DEMO-EMP-008: Cloud SRE & Kubernetes specialist (Kubernetes, Docker, Terraform, AWS)
INSERT INTO employees (employee_code, first_name, last_name, name, email, department, designation, location, joining_date, employment_status, max_capacity_percent, version, created_at, updated_at)
SELECT 'DEMO-EMP-008', 'Tariq', 'Mansour', 'Tariq Mansour', 'tariq.mansour.demo@talentmarketplace.com', 'DevOps & SRE', 'Lead DevOps Architect', 'London', '2020-02-01', 'ACTIVE', 100, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'DEMO-EMP-008');

-- DEMO-EMP-009: On leave employee (demonstrating status gating exclusion)
INSERT INTO employees (employee_code, first_name, last_name, name, email, department, designation, location, joining_date, employment_status, max_capacity_percent, version, created_at, updated_at)
SELECT 'DEMO-EMP-009', 'Maya', 'Lin', 'Maya Lin', 'maya.lin.demo@talentmarketplace.com', 'Engineering', 'Senior Fullstack Engineer', 'New York', '2021-10-05', 'ON_LEAVE', 100, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'DEMO-EMP-009');

-- DEMO-EMP-010: Inactive employee (demonstrating status gating exclusion)
INSERT INTO employees (employee_code, first_name, last_name, name, email, department, designation, location, joining_date, employment_status, max_capacity_percent, version, created_at, updated_at)
SELECT 'DEMO-EMP-010', 'Lucas', 'Dupont', 'Lucas Dupont', 'lucas.dupont.demo@talentmarketplace.com', 'Security', 'Security Specialist', 'Paris', '2023-05-18', 'INACTIVE', 100, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'DEMO-EMP-010');


-- ------------------------------------------------------------------------------
-- 3. SEED EMPLOYEE SKILLS (34 Skill Assignments with realistic proficiencies)
-- ------------------------------------------------------------------------------

-- DEMO-EMP-001 (Aria Stark): Java ADV, AWS ADV, Docker INT, Spring Boot ADV
INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 6.5, '2026-08-15', 'Principal Architect', 'Enterprise banking core migration lead', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-001' AND s.name = 'Java'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 5.0, '2026-08-15', 'Cloud Center of Excellence', 'AWS Certified Solutions Architect Professional', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-001' AND s.name = 'AWS'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'INTERMEDIATE', 4.0, '2026-07-10', 'DevOps Lead', 'Production containerized deployment experience', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-001' AND s.name = 'Docker'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 5.5, '2026-08-15', 'Principal Architect', 'Architected 20+ Spring Boot microservices', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-001' AND s.name = 'Spring Boot'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

-- DEMO-EMP-002 (Marcus Brody): Java ADV, Spring Boot ADV, Docker ADV, System Architecture INT (Lacks AWS)
INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 5.0, '2026-06-20', 'Engineering Manager', 'Senior Java Developer on High Throughput Services', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-002' AND s.name = 'Java'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 4.5, '2026-06-20', 'Engineering Manager', 'Spring Security and Data JPA lead', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-002' AND s.name = 'Spring Boot'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 4.0, '2026-05-15', 'DevOps Lead', 'Docker Compose and multi-stage build optimization', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-002' AND s.name = 'Docker'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'INTERMEDIATE', 3.0, '2026-05-15', 'Principal Architect', 'Design documentation and microservice boundaries', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-002' AND s.name = 'System Architecture'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

-- DEMO-EMP-003 (Elena Rostova): Java BEG, AWS INT, Docker BEG
INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'BEGINNER', 1.2, '2026-09-01', 'Team Lead', 'Completed Java 17 onboarding and junior tasks', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-003' AND s.name = 'Java'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'INTERMEDIATE', 2.0, '2026-09-01', 'Cloud Center of Excellence', 'AWS Certified Cloud Practitioner', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-003' AND s.name = 'AWS'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'BEGINNER', 1.0, '2026-09-01', 'Team Lead', 'Basic Docker run and container debugging', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-003' AND s.name = 'Docker'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

-- DEMO-EMP-004 (Devon Vance): Java ADV, AWS ADV, Spring Boot ADV, Docker ADV, Kubernetes INT
INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 6.0, '2026-07-25', 'Head of Tech', 'Lead developer on high availability core banking', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-004' AND s.name = 'Java'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 5.5, '2026-07-25', 'Cloud Architect', 'AWS ECS and EKS cloud infrastructure lead', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-004' AND s.name = 'AWS'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 5.0, '2026-07-25', 'Head of Tech', 'Spring Cloud gateway and reactive services', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-004' AND s.name = 'Spring Boot'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 4.5, '2026-07-25', 'DevOps Architect', 'Container security and image hardening', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-004' AND s.name = 'Docker'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'INTERMEDIATE', 3.0, '2026-07-25', 'DevOps Architect', 'Kubernetes Helm deployments', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-004' AND s.name = 'Kubernetes'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

-- DEMO-EMP-005 (Kavita Sharma): Java ADV, AWS INT, Docker INT, System Architecture ADV
INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 7.0, '2026-06-10', 'VP Engineering', 'Senior consultant with 7 years enterprise Java experience', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-005' AND s.name = 'Java'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'INTERMEDIATE', 3.5, '2026-06-10', 'Cloud Lead', 'Serverless architecture and S3/SQS integrations', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-005' AND s.name = 'AWS'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'INTERMEDIATE', 3.0, '2026-06-10', 'DevOps Lead', 'Standardized local dev environments', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-005' AND s.name = 'Docker'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 6.0, '2026-06-10', 'VP Engineering', 'Enterprise architectural governance board member', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-005' AND s.name = 'System Architecture'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

-- DEMO-EMP-006 (Liam Gallagher): React ADV, TypeScript ADV, GraphQL INT
INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 5.0, '2026-08-01', 'Design Director', 'Next.js and component design system creator', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-006' AND s.name = 'React'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 4.5, '2026-08-01', 'Frontend Lead', 'Strict TypeScript and state management specialist', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-006' AND s.name = 'TypeScript'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'INTERMEDIATE', 3.0, '2026-08-01', 'API Architect', 'Apollo client integration and cache optimization', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-006' AND s.name = 'GraphQL'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

-- DEMO-EMP-007 (Sofia Chen): Python ADV, SQL/PostgreSQL ADV, Kafka INT, AWS INT
INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 5.0, '2026-07-15', 'Head of Data', 'FastAPI and PySpark pipeline development', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-007' AND s.name = 'Python'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 5.5, '2026-07-15', 'Database Architect', 'PostgreSQL indexing, partitioning and query tuning', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-007' AND s.name = 'SQL/PostgreSQL'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'INTERMEDIATE', 2.5, '2026-07-15', 'Head of Data', 'Event-driven consumer groups and schema registry', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-007' AND s.name = 'Kafka'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'INTERMEDIATE', 3.0, '2026-07-15', 'Cloud Lead', 'AWS Redshift and EMR data analytics', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-007' AND s.name = 'AWS'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

-- DEMO-EMP-008 (Tariq Mansour): Kubernetes ADV, Docker ADV, Terraform INT, AWS ADV
INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 6.0, '2026-09-05', 'VP Infrastructure', 'CKA certified, production Kubernetes cluster manager', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-008' AND s.name = 'Kubernetes'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 5.5, '2026-09-05', 'VP Infrastructure', 'Enterprise container registry and CI/CD pipelines', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-008' AND s.name = 'Docker'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'INTERMEDIATE', 3.0, '2026-09-05', 'Lead SRE', 'Terraform modules for multi-region infrastructure', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-008' AND s.name = 'Terraform'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 5.0, '2026-09-05', 'VP Infrastructure', 'AWS Well-Architected Framework reviews', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-008' AND s.name = 'AWS'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

-- DEMO-EMP-009 (Maya Lin - On Leave): Java ADV, React ADV, AWS ADV
INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 5.0, '2026-05-01', 'Engineering Director', 'Fullstack fintech delivery lead', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-009' AND s.name = 'Java'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 4.5, '2026-05-01', 'Design Lead', 'Interactive web frontends in React', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-009' AND s.name = 'React'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 4.0, '2026-05-01', 'Cloud Architect', 'AWS serverless fullstack apps', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-009' AND s.name = 'AWS'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

-- DEMO-EMP-010 (Lucas Dupont - Inactive): Cybersecurity ADV, Python INT
INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'ADVANCED', 6.0, '2026-04-10', 'Chief Information Security Officer', 'CISSP certified, penetration testing lead', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-010' AND s.name = 'Cybersecurity'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);

INSERT INTO employee_skills (employee_id, skill_id, proficiency_level, years_of_experience, last_validated_at, validated_by, evidence_notes, created_at, updated_at)
SELECT e.id, s.id, 'INTERMEDIATE', 3.5, '2026-04-10', 'Security Lead', 'Automated security scanning tools in Python', NOW(), NOW()
FROM employees e, skills s WHERE e.employee_code = 'DEMO-EMP-010' AND s.name = 'Python'
AND NOT EXISTS (SELECT 1 FROM employee_skills es WHERE es.employee_id = e.id AND es.skill_id = s.id);


-- ------------------------------------------------------------------------------
-- 4. SEED PROJECT DEMANDS (5 Demands with future dates, varied priorities)
-- ------------------------------------------------------------------------------

-- DEMO-DEM-001: Core Target Demand for Scenario Testing
-- Dates: 2026-11-01 to 2026-12-31, 50% allocation, New York
INSERT INTO project_demands (demand_code, project_name, role, department, location, start_date, end_date, allocation_percent, priority, status, required_experience_years, created_at, updated_at)
SELECT 'DEMO-DEM-001', 'Demo Payments Modernization', 'Senior Cloud Java Developer', 'Engineering', 'New York', '2026-11-01', '2026-12-31', 50, 'HIGH', 'OPEN', 3.0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM project_demands WHERE demand_code = 'DEMO-DEM-001');

-- DEMO-DEM-002: Frontend NextGen Customer Portal (Remote, Critical)
INSERT INTO project_demands (demand_code, project_name, role, department, location, start_date, end_date, allocation_percent, priority, status, required_experience_years, created_at, updated_at)
SELECT 'DEMO-DEM-002', 'Demo NextGen Customer Portal', 'Lead Frontend React Engineer', 'Frontend & Mobile', 'Remote', '2026-12-01', '2027-02-28', 80, 'CRITICAL', 'OPEN', 4.0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM project_demands WHERE demand_code = 'DEMO-DEM-002');

-- DEMO-DEM-003: Real-Time Analytics Pipeline (San Francisco, Medium)
INSERT INTO project_demands (demand_code, project_name, role, department, location, start_date, end_date, allocation_percent, priority, status, required_experience_years, created_at, updated_at)
SELECT 'DEMO-DEM-003', 'Demo Real-Time Analytics Pipeline', 'Data Platform Specialist', 'Data Engineering', 'San Francisco', '2027-01-01', '2027-03-31', 60, 'MEDIUM', 'OPEN', 3.0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM project_demands WHERE demand_code = 'DEMO-DEM-003');

-- DEMO-DEM-004: Cloud Infrastructure & Kubernetes Migration (London, High)
INSERT INTO project_demands (demand_code, project_name, role, department, location, start_date, end_date, allocation_percent, priority, status, required_experience_years, created_at, updated_at)
SELECT 'DEMO-DEM-004', 'Demo Cloud Infrastructure Migration', 'DevOps & SRE Engineer', 'DevOps & SRE', 'London', '2026-11-15', '2027-01-31', 100, 'HIGH', 'OPEN', 4.0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM project_demands WHERE demand_code = 'DEMO-DEM-004');

-- DEMO-DEM-005: Enterprise Security Hardening (Remote, Low)
INSERT INTO project_demands (demand_code, project_name, role, department, location, start_date, end_date, allocation_percent, priority, status, required_experience_years, created_at, updated_at)
SELECT 'DEMO-DEM-005', 'Demo Enterprise Security Hardening', 'Security & Compliance Architect', 'Security', 'Remote', '2027-02-01', '2027-04-30', 50, 'LOW', 'OPEN', 5.0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM project_demands WHERE demand_code = 'DEMO-DEM-005');


-- ------------------------------------------------------------------------------
-- 5. SEED DEMAND SKILLS (Mandatory and Optional requirements per demand)
-- ------------------------------------------------------------------------------

-- Skills for DEMO-DEM-001 (Senior Cloud Java Developer):
-- Mandatory: Java (INTERMEDIATE), AWS (INTERMEDIATE)
-- Optional: Docker (BEGINNER), Spring Boot (INTERMEDIATE)
INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 1, 'INTERMEDIATE', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-001' AND s.name = 'Java'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 1, 'INTERMEDIATE', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-001' AND s.name = 'AWS'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 0, 'BEGINNER', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-001' AND s.name = 'Docker'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 0, 'INTERMEDIATE', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-001' AND s.name = 'Spring Boot'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

-- Skills for DEMO-DEM-002 (Lead Frontend React Engineer):
-- Mandatory: React (ADVANCED), TypeScript (INTERMEDIATE)
-- Optional: GraphQL (BEGINNER)
INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 1, 'ADVANCED', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-002' AND s.name = 'React'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 1, 'INTERMEDIATE', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-002' AND s.name = 'TypeScript'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 0, 'BEGINNER', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-002' AND s.name = 'GraphQL'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

-- Skills for DEMO-DEM-003 (Data Platform Specialist):
-- Mandatory: Python (INTERMEDIATE), SQL/PostgreSQL (INTERMEDIATE)
-- Optional: Kafka (BEGINNER)
INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 1, 'INTERMEDIATE', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-003' AND s.name = 'Python'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 1, 'INTERMEDIATE', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-003' AND s.name = 'SQL/PostgreSQL'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 0, 'BEGINNER', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-003' AND s.name = 'Kafka'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

-- Skills for DEMO-DEM-004 (DevOps & SRE Engineer):
-- Mandatory: Kubernetes (ADVANCED), Docker (INTERMEDIATE)
-- Optional: Terraform (BEGINNER), AWS (INTERMEDIATE)
INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 1, 'ADVANCED', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-004' AND s.name = 'Kubernetes'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 1, 'INTERMEDIATE', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-004' AND s.name = 'Docker'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 0, 'BEGINNER', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-004' AND s.name = 'Terraform'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 0, 'INTERMEDIATE', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-004' AND s.name = 'AWS'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

-- Skills for DEMO-DEM-005 (Security & Compliance Architect):
-- Mandatory: Cybersecurity (ADVANCED)
-- Optional: System Architecture (INTERMEDIATE)
INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 1, 'ADVANCED', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-005' AND s.name = 'Cybersecurity'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);

INSERT INTO demand_skills (demand_id, skill_id, mandatory, minimum_proficiency, created_at, updated_at)
SELECT d.id, s.id, 0, 'INTERMEDIATE', NOW(), NOW()
FROM project_demands d, skills s WHERE d.demand_code = 'DEMO-DEM-005' AND s.name = 'System Architecture'
AND NOT EXISTS (SELECT 1 FROM demand_skills ds WHERE ds.demand_id = d.id AND ds.skill_id = s.id);


-- ------------------------------------------------------------------------------
-- 6. SEED EMPLOYEE UNAVAILABILITIES (3 Records for realistic availability checks)
-- ------------------------------------------------------------------------------

-- Overlapping leave for DEMO-EMP-004 during DEMO-DEM-001 (2026-11-10 to 2026-11-20)
INSERT INTO employee_unavailabilities (employee_id, start_date, end_date, type, reason, created_at, updated_at)
SELECT e.id, '2026-11-10', '2026-11-20', 'ANNUAL_LEAVE', 'Demo scheduled annual family vacation', NOW(), NOW()
FROM employees e WHERE e.employee_code = 'DEMO-EMP-004'
AND NOT EXISTS (
    SELECT 1 FROM employee_unavailabilities eu
    WHERE eu.employee_id = e.id AND eu.start_date = '2026-11-10' AND eu.end_date = '2026-11-20' AND eu.type = 'ANNUAL_LEAVE'
);

-- Training leave for DEMO-EMP-007 (2026-11-05 to 2026-11-12) - outside DEMO-DEM-003 period
INSERT INTO employee_unavailabilities (employee_id, start_date, end_date, type, reason, created_at, updated_at)
SELECT e.id, '2026-11-05', '2026-11-12', 'TRAINING', 'Demo Advanced Distributed Kafka Systems Workshop', NOW(), NOW()
FROM employees e WHERE e.employee_code = 'DEMO-EMP-007'
AND NOT EXISTS (
    SELECT 1 FROM employee_unavailabilities eu
    WHERE eu.employee_id = e.id AND eu.start_date = '2026-11-05' AND eu.end_date = '2026-11-12' AND eu.type = 'TRAINING'
);

-- Holiday leave for DEMO-EMP-001 (2027-01-15 to 2027-01-22) - outside DEMO-DEM-001 period
INSERT INTO employee_unavailabilities (employee_id, start_date, end_date, type, reason, created_at, updated_at)
SELECT e.id, '2027-01-15', '2027-01-22', 'ANNUAL_LEAVE', 'Demo scheduled winter holiday break', NOW(), NOW()
FROM employees e WHERE e.employee_code = 'DEMO-EMP-001'
AND NOT EXISTS (
    SELECT 1 FROM employee_unavailabilities eu
    WHERE eu.employee_id = e.id AND eu.start_date = '2027-01-15' AND eu.end_date = '2027-01-22' AND eu.type = 'ANNUAL_LEAVE'
);
