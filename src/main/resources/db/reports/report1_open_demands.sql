-- ==============================================================================
-- REPORT 1: Open Demands by Role and Priority
-- File: src/main/resources/db/reports/report1_open_demands.sql
-- Endpoint: GET /api/analytics/reports/open-demands-by-role-priority
-- Description: Aggregates all project demands currently in 'OPEN' status,
--              grouped by role and priority level.
-- ==============================================================================

SELECT 
    d.role AS role,
    d.priority AS priority,
    COUNT(d.id) AS demand_count,
    COALESCE(SUM(d.allocation_percent), 0) AS total_allocation_percent,
    MIN(d.start_date) AS earliest_start_date,
    MAX(d.end_date) AS latest_end_date
FROM project_demands d
WHERE d.status = 'OPEN'
GROUP BY d.role, d.priority
ORDER BY d.role ASC, 
    CASE d.priority
        WHEN 'CRITICAL' THEN 1
        WHEN 'HIGH' THEN 2
        WHEN 'MEDIUM' THEN 3
        WHEN 'LOW' THEN 4
        ELSE 5
    END ASC;
