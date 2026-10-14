-- ==============================================================================
-- REPORT 2: Resources by Skill and Proficiency
-- File: src/main/resources/db/reports/report2_resources_by_skill.sql
-- Endpoint: GET /api/analytics/reports/resources-by-skill-proficiency
-- Description: Aggregates active employees possessing each skill at each
--              proficiency level. Uses COUNT(DISTINCT e.id) to guarantee
--              no duplicate counts.
-- ==============================================================================

SELECT 
    s.id AS skill_id,
    s.name AS skill_name,
    s.category AS category,
    es.proficiency_level AS proficiency_level,
    COUNT(DISTINCT e.id) AS resource_count,
    COALESCE(ROUND(AVG(es.years_of_experience), 1), 0.0) AS avg_years_experience
FROM skills s
JOIN employee_skills es ON s.id = es.skill_id
JOIN employees e ON es.employee_id = e.id
WHERE e.employment_status = 'ACTIVE'
  AND (:skillId IS NULL OR s.id = :skillId)
  AND (:category IS NULL OR s.category = :category)
  AND (:proficiencyLevel IS NULL OR es.proficiency_level = :proficiencyLevel)
GROUP BY s.id, s.name, s.category, es.proficiency_level
ORDER BY s.category ASC, s.name ASC, 
    CASE es.proficiency_level
        WHEN 'ADVANCED' THEN 1
        WHEN 'INTERMEDIATE' THEN 2
        WHEN 'BEGINNER' THEN 3
        ELSE 4
    END ASC;
