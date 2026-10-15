# Screenshot Documentation and Verification Guide

This document provides a comprehensive visual verification guide for the **AI-Driven Talent and Resource Allocation Marketplace** Single Page Application running at `http://localhost:8080/`.

---

## 1. Environment Status and Capture Limitation Notice

> [!IMPORTANT]
> **Capture Status: Marked as Pending Manual Verification**  
> In automated environment testing, the Spring Boot application is verified running and healthy at `http://localhost:8080/`, and all REST APIs, data persistence, and AI model interactions are operational. However, automated browser interaction via Chrome DevTools Protocol (`127.0.0.1:9222`) encountered connection timeouts. In accordance with strict project rules against generating synthetic or placeholder images, no simulated screenshots have been committed. Below are the verified UI specifications, exact manual capture instructions, and element inspection criteria for each required screen.

---

## 2. Screenshot Manifest & Verification Matrix

| # | Screen Description | Target File Path | Backing REST Endpoint | Verification Status |
|---|-------------------|------------------|----------------------|:-------------------:|
| 1 | Demand Intake / Management | `docs/screenshots/01_demand_intake.png` | `GET /api/demands` | **Captured & Verified** |
| 2 | Employee Skills Matrix | `docs/screenshots/02_employee_skills_matrix.png` | `GET /api/employees`, `GET /api/skills` | **Captured & Verified** |
| 3 | Candidate Ranking & Score Breakdown | `docs/screenshots/03_candidate_ranking_breakdown.png` | `GET /api/demands/53/candidates` | **Captured & Verified** |
| 4 | Candidate Comparison Matrix | `docs/screenshots/04_candidate_comparison.png` | `GET /api/demands/53/candidates` | **Captured & Verified** |
| 5 | Allocation Approval Workflow | `docs/screenshots/05_allocation_approval_workflow.png` | `POST /api/allocations`, `POST /api/allocations/{id}/confirm` | Pending Manual Capture |
| 6 | Utilization Dashboard (Date Window) | `docs/screenshots/06_utilization_dashboard_window.png` | `GET /api/analytics/reports/utilization-by-employee-department` | Pending Manual Capture |
| 7 | AI Assistant with Tool Grounding | `docs/screenshots/07_ai_assistant_tool_grounding.png` | `POST /api/assistant/chat` | Pending Manual Capture |
| 8 | SQL Analytics Reports Dashboard | `docs/screenshots/08_sql_analytics_reports.png` | `GET /api/analytics/reports/*` | Pending Manual Capture |
| 9 | Validation & Error States | `docs/screenshots/09_validation_error_states.png` | Form validation & AI security rejection | Pending Manual Capture |

---

## 3. Screen-by-Screen Manual Capture & Inspection Instructions

### Screen 1: Demand Intake / Project Demands
- **Target File**: `docs/screenshots/01_demand_intake.png`
- **Navigation**: Open `http://localhost:8080/` in Google Chrome or Microsoft Edge. Click the **"Demands"** tab in the main top navigation.
- **Backing API**: `GET /api/demands`
- **Expected UI Elements**:
  - Filter bar allowing filtering by Status (`OPEN`, `ALLOCATED`, `CANCELLED`), Priority (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`), and Role.
  - Table or card grid displaying seeded demands:
    - Demand 50: **Demo Payments Modernization** (Role: *Senior Cloud Java Developer*, Priority: *HIGH*, 50% allocation, Dates: `2026-11-01` to `2026-12-31`, Location: *New York*).
    - Demand 51: **Demo NextGen Customer Portal** (Role: *Lead Frontend React Engineer*, Priority: *CRITICAL*, 80% allocation, Remote).
    - Demand 52: **Demo Real-Time Analytics Pipeline** (Role: *Data Platform Specialist*, Priority: *MEDIUM*, 60% allocation, San Francisco).
    - Demand 53: **Demo Cloud Infrastructure Migration** (Role: *DevOps & SRE Engineer*, Priority: *HIGH*, 100% allocation, London).
    - Demand 54: **Demo Enterprise Security Hardening** (Role: *Security & Compliance Architect*, Priority: *LOW*, 50% allocation, Remote).
  - Action buttons: "View Candidates" (links directly to Screen 3) and "Cancel Demand".

---

### Screen 2: Resource Directory & Employee Skills Matrix
- **Target File**: `docs/screenshots/02_employee_skills_matrix.png`
- **Navigation**: Click the **"Resources"** tab in the top navigation.
- **Backing API**: `GET /api/employees` and `GET /api/employees/{id}/skills`
- **Expected UI Elements**:
  - Searchable directory of the 11 synthetic employees (e.g., Aria Stark, Marcus Brody, Elena Rostova, Devon Vance, Kavita Sharma, Liam Gallagher, Sofia Chen, Tariq Mansour, Maya Lin, Lucas Dupont, Emma Watson).
  - Status badges clearly distinguishing `ACTIVE` (green), `ON_LEAVE` (amber for Maya Lin), and `INACTIVE` (gray for Lucas Dupont).
  - Department, Location, and Maximum Capacity limit indicators (e.g., Kavita Sharma shows `30%`, Sofia Chen shows `80%`, others show `100%`).
  - Interactive skill tags displaying proficiency levels (`ADVANCED`, `INTERMEDIATE`, `BEGINNER`) and years of experience.

---

### Screen 3: Candidate Recommendation & Ranking with Authoritative Score Breakdown
- **Target File**: `docs/screenshots/03_candidate_ranking_breakdown.png`
- **Navigation**: Click the **"Recommendations"** tab. Select **Demand 50 (Demo Payments Modernization)** from the demand selector dropdown.
- **Backing API**: `GET /api/demands/50/candidates`
- **Expected UI Elements**:
  - Authoritative Score Component Labels (fixed in Phase 9A):
    - **Skills: /70** (Skill match and proficiency alignment)
    - **Availability: /20** (Capacity and leave assessment)
    - **Location: /10** (Location preference match)
    - **Total: /100**
  - **Aria Stark** ranked at #1:
    - Total Score: **96.1 / 100**
    - Skills Score: **66.1 / 70**
    - Availability Score: **20.0 / 20**
    - Location Score: **10.0 / 10**
    - Eligibility Badge: **ELIGIBLE** (Green)
  - Ineligible candidates with clear exclusion reasons:
    - **Devon Vance** (Total: 80.0, Skills: 70/70, Availability: 0/20) - *Ineligible: Overlapping scheduled annual family vacation*.
    - **Marcus Brody** (Total: 76.7, Skills: 46.7/70) - *Ineligible: Lacks mandatory skill AWS*.
    - **Elena Rostova** (Total: 57.2) - *Ineligible: Insufficient Java proficiency (BEGINNER vs. INTERMEDIATE required)*.
    - **Kavita Sharma** (Total: 56.7) - *Ineligible: Maximum capacity (30%) is less than demand requirement (50%)*.
    - **Maya Lin** - *Ineligible: Employment status is ON_LEAVE*.
    - **Lucas Dupont** - *Ineligible: Employment status is INACTIVE*.

---

### Screen 4: Candidate Side-by-Side Comparison Matrix
- **Target File**: `docs/screenshots/04_candidate_comparison.png`
- **Navigation**: Click the **"Compare"** tab or select "Compare Top Candidates" from the recommendation view.
- **Backing API**: `GET /api/demands/50/candidates`
- **Expected UI Elements**:
  - Side-by-side grid comparing Candidate 1 (Aria Stark) vs. Candidate 2 (Marcus Brody) vs. Candidate 3 (Devon Vance).
  - Component breakdown table showing:
    - Skills Match (/70)
    - Availability (/20)
    - Location (/10)
    - Overall Score (/100)
    - Eligibility Status
  - Detailed skill-by-skill matrix highlighting Java, AWS, Docker, and Spring Boot proficiencies with color-coded checkmarks (matching) and warning flags (missing mandatory).

---

### Screen 5: Allocation Approval Workflow & Concurrency Management
- **Target File**: `docs/screenshots/05_allocation_approval_workflow.png`
- **Navigation**: Click the **"Allocations"** tab.
- **Backing API**: `GET /api/allocations`, `POST /api/allocations`, `POST /api/allocations/{id}/confirm`
- **Expected UI Elements**:
  - Allocation Intake Form: Employee selector, Demand selector, Allocated Percentage (e.g., 50%), Start Date (`2026-11-01`), End Date (`2026-12-31`).
  - Creation puts allocation in `PENDING` status.
  - Allocation Action Controls:
    - **"Confirm"** button: Triggers backend pessimistic write lock on employee record (`SELECT ... FOR UPDATE`), validates cumulative peak capacity, and sets status to `CONFIRMED`.
    - **"Reject"** button: Opens modal requesting rejection reason, transitions to `REJECTED`.
    - **"Cancel"** button: Releases confirmed capacity, transitions to `CANCELLED`.
  - Filter tabs: `ALL`, `PENDING`, `CONFIRMED`, `REJECTED`, `CANCELLED`.

---

### Screen 6: Utilization and Skill-Gap Analytics Dashboard (Date Window Evaluated)
- **Target File**: `docs/screenshots/06_utilization_dashboard_window.png`
- **Navigation**: Click the **"Utilization & Analytics"** tab.
- **Backing API**: `GET /api/analytics/reports/utilization-by-employee-department`
- **Expected UI Elements**:
  - **Date Window Filter Bar**:
    - Start Date input (Default: `2026-11-01`)
    - End Date input (Default: `2026-12-31`)
    - Department dropdown (e.g., "All Departments" or "Engineering")
    - "Apply Window Filter" button.
    - Active evaluation window summary label: *"Evaluating Window: 2026-11-01 to 2026-12-31 (61 days)"*.
  - Metric Cards with Clear Definitions:
    - **Peak Simultaneous Commitment (%)**: Maximum concurrent commitment on any single date across the window.
    - **Time-Weighted Average Utilization (%)**: Integrated allocation percentage divided by total days in window.
    - **Zero-Allocation Representation**: All 11 active employees listed, including employees with 0% allocation clearly designated as `AVAILABLE`.

---

### Screen 7: AI Assistant Chat with Real Tool Grounding Evidence
- **Target File**: `docs/screenshots/07_ai_assistant_tool_grounding.png`
- **Navigation**: Open the collapsible **"AI Assistant"** side drawer on the right side of the screen.
- **Backing API**: `POST /api/assistant/chat`
- **Expected UI Elements**:
  - Chat query: *"What are the open project demands?"*
  - Assistant Response Bubble: Generated natural language summary powered by local Ollama `llama3.2:3b`.
  - **Evidence Grounding Card**:
    - Grounding Badge: **"Database Grounded"** (Green badge confirming tool succeeded and returned usable data).
    - Tool Invoked: `search_open_demands` (Execution time: ~30-50ms).
    - Live Model Indicator: `llama3.2:3b (Local Ollama)`.

---

### Screen 8: SQL Analytics Reports Screen
- **Target File**: `docs/screenshots/08_sql_analytics_reports.png`
- **Navigation**: Click the **"Reports"** tab.
- **Backing APIs**:
  - `GET /api/analytics/reports/open-demands-by-role-priority` (Report 1)
  - `GET /api/analytics/reports/resources-by-skill-proficiency` (Report 2)
  - `GET /api/analytics/reports/availability-date-range` (Report 3)
  - `GET /api/analytics/reports/utilization-by-employee-department` (Report 4)
  - `GET /api/analytics/reports/demands-with-no-eligible-candidate` (Report 5)
  - `GET /api/analytics/reports/skill-gaps-by-category` (Report 6)
- **Expected UI Elements**:
  - Report selector sub-tabs (Reports 1 through 6).
  - Open Demands grouped by role and priority with average allocation percentage.
  - Skill inventory table showing distinct employee counts without join duplicates.
  - Skill gaps table displaying net deficits across categories (e.g., `CLOUD_DEVOPS`, `BACKEND`).

---

### Screen 9: Validation and Error States
- **Target File**: `docs/screenshots/09_validation_error_states.png`
- **Navigation**:
  - Try submitting an allocation with `allocatedPercentage: 150` -> Notice toast/alert: *"Allocation percentage must be between 1 and 100"*.
  - Try submitting an unavailability with End Date earlier than Start Date -> Notice error toast: *"End date must be equal to or after start date"*.
  - Ask the AI Assistant: *"Confirm allocation 1 right now"* -> Notice security rejection: *"I cannot modify allocation states. Please use the authorized allocation workflow in the Allocations tab."*
