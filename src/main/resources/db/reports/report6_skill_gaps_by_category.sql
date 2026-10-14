-- ==============================================================================
-- REPORT 6: Skill Gaps by Category
-- File: src/main/resources/db/reports/report6_skill_gaps_by_category.sql
-- Endpoint: GET /api/analytics/reports/skill-gaps-by-category
-- Description: Aggregates open-demand skill requirements by category and compares
--              against active qualified resource supply.
--
-- ARCHITECTURE NOTE:
-- - SQL Portion:
--   1. Aggregates open-demand requirements per category and skill with mandatory vs optional split.
--   2. Aggregates active employee supply per skill and proficiency level without double counting
--      (COUNT(DISTINCT e.id)).
-- - Java Portion: Compares demand against qualified supply according to proficiency hierarchy
--   (EXPERT >= ADVANCED >= INTERMEDIATE >= BEGINNER) to calculate exact net deficits.
-- ==============================================================================

-- 6A: Skill Demand Requirements from Open Demands
SELECT 
    s.category AS category,
    s.id AS skill_id,
    s.name AS skill_name,
    ds.minimum_proficiency AS minimum_proficiency,
    COUNT(DISTINCT d.id) AS demand_count,
    SUM(CASE WHEN ds.mandatory = true THEN 1 ELSE 0 END) AS mandatory_count,
    SUM(CASE WHEN ds.mandatory = false THEN 1 ELSE 0 END) AS optional_count
FROM demand_skills ds
JOIN skills s ON ds.skill_id = s.id
JOIN project_demands d ON ds.demand_id = d.id
WHERE d.status = 'OPEN'
  AND (:category IS NULL OR s.category = :category)
GROUP BY s.category, s.id, s.name, ds.minimum_proficiency
ORDER BY s.category ASC, s.name ASC;

-- 6B: Active Employee Supply per Skill and Proficiency Level
SELECT 
    s.category AS category,
    s.id AS skill_id,
    s.name AS skill_name,
    es.proficiency_level AS proficiency_level,
    COUNT(DISTINCT e.id) AS resource_count
FROM employee_skills es
JOIN skills s ON es.skill_id = s.id
JOIN employees e ON es.employee_id = e.id
WHERE e.employment_status = 'ACTIVE'
  AND (:category IS NULL OR s.category = :category)
GROUP BY s.category, s.id, s.name, es.proficiency_level
ORDER BY s.category ASC, s.name ASC;
