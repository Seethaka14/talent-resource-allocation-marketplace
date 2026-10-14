-- ==============================================================================
-- REPORT 5: Demands with No Eligible Candidate
-- File: src/main/resources/db/reports/report5_demands_no_eligible_candidate.sql
-- Endpoint: GET /api/analytics/reports/demands-with-no-eligible-candidate
-- Description: Identifies open project demands for which zero active resources
--              qualify across all deterministic eligibility gates.
--
-- ARCHITECTURE NOTE:
-- - SQL Portion: Efficiently queries all open demands (status = 'OPEN') and pre-aggregates
--   required skill counts and priority ranking.
-- - Java Portion: Executes the authoritative recommendation engine (CandidateRecommendationService)
--   which checks:
--     1. Mandatory skill possession
--     2. Minimum proficiency requirements (EXPERT > ADVANCED > INTERMEDIATE > BEGINNER)
--     3. Active employment status
--     4. Overlapping PTO/unavailabilities
--     5. Available capacity >= demand requested allocation percent
--   Demands with 0 eligible candidates are compiled into the diagnostic unfulfillable report.
-- ==============================================================================

SELECT 
    d.id AS demand_id,
    d.demand_code AS demand_code,
    d.project_name AS project_name,
    d.role AS role,
    d.department AS department,
    d.location AS location,
    d.start_date AS start_date,
    d.end_date AS end_date,
    d.allocation_percent AS allocation_percent,
    d.priority AS priority,
    COUNT(ds.id) AS total_required_skills,
    SUM(CASE WHEN ds.mandatory = true THEN 1 ELSE 0 END) AS mandatory_skills_count
FROM project_demands d
LEFT JOIN demand_skills ds ON d.id = ds.demand_id
WHERE d.status = 'OPEN'
GROUP BY d.id, d.demand_code, d.project_name, d.role, d.department, d.location, 
         d.start_date, d.end_date, d.allocation_percent, d.priority
ORDER BY 
    CASE d.priority
        WHEN 'CRITICAL' THEN 1
        WHEN 'HIGH' THEN 2
        WHEN 'MEDIUM' THEN 3
        WHEN 'LOW' THEN 4
        ELSE 5
    END ASC,
    d.start_date ASC;
