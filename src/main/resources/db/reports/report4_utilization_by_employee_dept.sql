-- ==============================================================================
-- REPORT 4: Utilization by Employee and Department
-- File: src/main/resources/db/reports/report4_utilization_by_employee_dept.sql
-- Endpoint: GET /api/analytics/reports/utilization-by-employee-department
-- Description: Measures employee and departmental utilization over a defined evaluation
--              window [startDate, endDate].
--
-- EXACT METRIC DEFINITION:
-- 1. Evaluated Window: Defined by startDate and endDate (D total days = endDate - startDate + 1).
-- 2. Peak Simultaneous Utilization: The maximum simultaneous allocation percentage across all
--    confirmed allocations active on any single day t within [startDate, endDate], relative to
--    the employee's configured maxCapacityPercent.
-- 3. Time-Weighted Average Utilization: The sum of daily confirmed allocation percentages
--    across all D days divided by (D * maxCapacityPercent). Allocations spanning only a portion
--    of the window are properly time-proportioned rather than naively summed.
-- 4. Zero-Allocation Employees: Included with 0% peak and 0% average utilization.
-- 5. Aggregate Allocation Count: Total count of distinct confirmed allocation records intersecting
--    the reporting window.
-- ==============================================================================

-- 4A: Retrieve all active employees (ensures employees with 0 allocations are included)
SELECT 
    e.id AS employee_id,
    e.employee_code AS employee_code,
    e.name AS employee_name,
    e.department AS department,
    e.max_capacity_percent AS max_capacity_percent
FROM employees e
WHERE e.employment_status = 'ACTIVE'
  AND (:department IS NULL OR e.department = :department)
ORDER BY e.department ASC, e.name ASC;

-- 4B: Retrieve overlapping CONFIRMED allocations within the evaluation window
SELECT 
    ra.id AS allocation_id,
    ra.employee_id AS employee_id,
    ra.demand_id AS demand_id,
    ra.start_date AS start_date,
    ra.end_date AS end_date,
    ra.allocation_percent AS allocation_percent,
    ra.status AS status
FROM resource_allocations ra
WHERE ra.status = 'CONFIRMED'
  AND ra.start_date <= :endDate
  AND ra.end_date >= :startDate
ORDER BY ra.employee_id ASC, ra.start_date ASC;
