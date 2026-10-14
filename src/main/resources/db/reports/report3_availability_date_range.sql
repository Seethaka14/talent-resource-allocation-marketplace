-- ==============================================================================
-- REPORT 3: Availability for a Date Range
-- File: src/main/resources/db/reports/report3_availability_date_range.sql
-- Endpoint: GET /api/analytics/reports/availability-date-range
-- Description: Retrieves active employees with their overlapping unavailabilities
--              and confirmed allocations over an inclusive date range [startDate, endDate].
--
-- ARCHITECTURE NOTE:
-- - SQL Portion: Efficiently queries active employees, their configured max capacity,
--   and identifies intersecting confirmed allocations (status = 'CONFIRMED') and
--   unavailabilities (status/dates) within the reporting window.
-- - Java Portion: Evaluates peak simultaneous allocation across discrete timeline
--   transition points (critical dates) and intersects with scheduled leave to compute
--   precise net remaining capacity and availability status.
-- ==============================================================================

-- 3A: Candidate Active Employees for the Window
SELECT 
    e.id AS employee_id,
    e.employee_code AS employee_code,
    e.name AS employee_name,
    e.department AS department,
    e.location AS location,
    e.employment_status AS employment_status,
    e.max_capacity_percent AS max_capacity_percent
FROM employees e
WHERE e.employment_status = 'ACTIVE'
  AND (:department IS NULL OR e.department = :department)
ORDER BY e.department ASC, e.name ASC;

-- 3B: Overlapping Confirmed Allocations in Date Range (Only CONFIRMED consume capacity)
SELECT 
    ra.id AS allocation_id,
    ra.employee_id AS employee_id,
    ra.start_date AS start_date,
    ra.end_date AS end_date,
    ra.allocation_percent AS allocation_percent,
    ra.status AS status
FROM resource_allocations ra
WHERE ra.status = 'CONFIRMED'
  AND ra.start_date <= :endDate
  AND ra.end_date >= :startDate;

-- 3C: Overlapping Scheduled Unavailabilities in Date Range
SELECT 
    eu.id AS unavailability_id,
    eu.employee_id AS employee_id,
    eu.start_date AS start_date,
    eu.end_date AS end_date,
    eu.type AS unavailability_type,
    eu.reason AS reason
FROM employee_unavailabilities eu
WHERE eu.start_date <= :endDate
  AND eu.end_date >= :startDate;
