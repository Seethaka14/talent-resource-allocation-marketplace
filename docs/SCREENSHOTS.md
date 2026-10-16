# Screenshot Documentation and Verification Guide

This document provides a comprehensive visual verification guide for the **AI-Driven Talent and Resource Allocation Marketplace** Single Page Application running at `http://localhost:8080/`.

---

## 1. Environment Status and UI Architecture Verification

> [!NOTE]
> **UI Architecture Update: Fixed Left Sidebar & Mobile Drawer Navigation**  
> The application has been refactored to replace top horizontal navigation tabs with a professional, fixed left-side vertical navigation sidebar (~250px wide) on desktop, which collapses into an off-canvas drawer with backdrop blur on mobile devices (`<= 992px`). All existing dark/purple visual styling, REST APIs, candidate scoring formula (Skills /70, Availability /20, Location /10), and deterministic backend logic are 100% preserved. Zero emojis are used across the interface.

---

## 2. Screenshot Manifest & Verification Matrix

| # | Screen Description | Target File Path | Backing REST Endpoint / Function | Verification Status |
|---|-------------------|------------------|----------------------------------|:-------------------:|
| 0 | Marketplace Executive Dashboard | `docs/screenshots/00_Marketplace_executive_dashboard.png` | `GET /api/demands`, `GET /api/employees`, `GET /api/allocations` | **Captured & Verified** |
| 1 | Demand Intake / Project Demands | `docs/screenshots/01_Demand_intake.png` | `GET /api/demands`, `POST /api/demands` | **Captured & Verified** |
| 2 | Employee Skills Matrix | `docs/screenshots/02_Employee_skill_matrix.png` | `GET /api/employees`, `GET /api/skills` | **Captured & Verified** |
| 3 | Candidate Ranking & Score Breakdown | `docs/screenshots/03_Resource_ranking.png` | `GET /api/demands/50/candidates` | **Captured & Verified** |
| 4 | Candidate Comparison Matrix | `docs/screenshots/04_Candidate_comparision.png` | `GET /api/demands/50/candidates` | **Captured & Verified** |
| 5 | Allocation Approval Workflow | `docs/screenshots/05_Resource_approvals.png` | `POST /api/allocations`, `POST /api/allocations/{id}/confirm` | **Captured & Verified** |
| 6 | Utilization Dashboard (Date Window) | `docs/screenshots/06_Resource_utilization.png` | `GET /api/analytics/reports/utilization-by-employee-department` | **Captured & Verified** |
| 7 | Enterprise Skill Shortage Analysis | `docs/screenshots/07_Skill_gap_analysis.png` | `GET /api/analytics/reports/skill-gaps-by-category` | **Captured & Verified** |
| 8 | AI Staffing Agent Assistant Panel | `docs/screenshots/08_AI_staffing_agent_assistant.png` | `POST /api/assistant/chat`, `GET /api/assistant/status` | **Captured & Verified** |
| 9 | Maven Automated Test Execution (223 Tests) | `docs/screenshots/Test_results.png` | `.\mvnw.cmd test` | **Captured & Verified** |

---

## 3. Screen-by-Screen UI Specification & Inspection Criteria

### Screen 0: Marketplace Executive Dashboard
- **File**: `docs/screenshots/00_Marketplace_executive_dashboard.png`
- **Navigation**: Click **"Dashboard"** in the left sidebar.
- **Backing APIs**: `GET /api/demands`, `GET /api/employees`, `GET /api/allocations`
- **Expected UI Elements**:
  - Left navigation sidebar (~250px) with brand badge (`TRM`), section headings (`OVERVIEW`, `TALENT & ALLOCATIONS`, `INTELLIGENCE`), and active highlight on `Dashboard`.
  - 4 High-level KPI metric cards:
    - **Active Talent Headcount**: 11 active resources across Engineering & Platform.
    - **Open Project Demands**: 5 demands pending allocation.
    - **Active Allocations**: Active confirmed and pending allocations count.
    - **AI Staffing Engine**: Live status indicator for `llama3.2:3b` with 6 grounded tools.
  - Interactive Launchpad grid cards for one-click navigation to key workflows.
  - Live data tables previewing open project demands and enterprise talent.

---

### Screen 1: Project Demand Intake
- **File**: `docs/screenshots/01_Demand_intake.png`
- **Navigation**: Click **"Project Demands"** in the left sidebar.
- **Backing API**: `GET /api/demands`, `POST /api/demands`
- **Expected UI Elements**:
  - Two-column responsive layout:
    - **Left**: Demand Creation Form with auto-generating code generator, role, department, location, priority (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`), dates, allocation percentage (1-100%), required experience, and interactive skill requirements builder with proficiency levels (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`) and mandatory toggles.
    - **Right**: Open Demands card list showing seeded demands (e.g., `#50 Senior Cloud Java Developer`, `#51 Lead Frontend React Engineer`, `#52 Data Platform Specialist`, `#53 DevOps & SRE Engineer`, `#54 Security Architect`) with priority badges, skill chips, and direct action buttons ("Rank Candidates", "Ask AI").

---

### Screen 2: Employee Skills Matrix
- **File**: `docs/screenshots/02_Employee_skill_matrix.png`
- **Navigation**: Click **"Employee Skills Matrix"** in the left sidebar.
- **Backing API**: `GET /api/employees` and `GET /api/employees/{id}/skills`
- **Expected UI Elements**:
  - Multi-criteria filter bar: text search (name/code), Department dropdown, Skill dropdown, Minimum Proficiency filter, and Employment Status (`ACTIVE`, `ON_LEAVE`, `NOTICE_PERIOD`, `TERMINATED`).
  - Roster Table inside a responsive container displaying employee codes, roles, statuses (e.g. `ACTIVE`, `ON_LEAVE` for Maya Lin), maximum capacities, verified skill chips with years of experience, and quick "Inquire" actions.

---

### Screen 3: Candidate Recommendation & Ranking with Authoritative Score Breakdown
- **File**: `docs/screenshots/03_Resource_ranking.png`
- **Navigation**: Click **"Resource Ranking"** in the left sidebar. Select a target demand (e.g., Demand 50).
- **Backing API**: `GET /api/demands/50/candidates`
- **Expected UI Elements**:
  - Demand selector dropdown with minimum score slider and "Eligible Only" checkbox filter.
  - Authoritative Score Breakdown Formula displayed on every candidate card:
    - **Skills: /70** (Mandatory + optional skills matching)
    - **Availability: /20** (Capacity and leave overlap check)
    - **Location: /10** (Location preference match)
    - **Total: /100**
  - Top Candidate (Aria Stark): Total 96.1/100, Eligible (Green badge).
  - Ineligible Candidates with explicit exclusion reasons (e.g. Devon Vance: overlapping leave, Marcus Brody: missing mandatory AWS, Kavita Sharma: max capacity 30% < 50% required).

---

### Screen 4: Candidate Side-by-Side Comparison Matrix
- **File**: `docs/screenshots/04_Candidate_comparision.png`
- **Navigation**: Click **"Candidate Comparison"** in the left sidebar or click "Compare Selected" from Screen 3.
- **Backing API**: `GET /api/demands/50/candidates`
- **Expected UI Elements**:
  - Demand selector and quick comparison buttons ("Compare Top 2", "Compare Top 3").
  - Side-by-side comparison matrix comparing up to 3 candidates across score components (`/70`, `/20`, `/10`), eligibility status, department, location, availability windows, and skill-by-skill proficiency comparison.

---

### Screen 5: Allocation Approval Workflow & Concurrency Management
- **File**: `docs/screenshots/05_Resource_approvals.png`
- **Navigation**: Click **"Allocation Approvals"** in the left sidebar.
- **Backing API**: `GET /api/allocations`, `POST /api/allocations`, `POST /api/allocations/{id}/confirm`
- **Expected UI Elements**:
  - "+ Request Allocation" modal button and status filter pills (`ALL`, `PENDING`, `CONFIRMED`, `REJECTED`, `CANCELLED`).
  - Allocation Records table displaying Allocation ID, Status, Employee, Demand, Allocated Percentage, Date Range, and Lifecycle Actions:
    - **Confirm**: Triggers pessimistic row lock (`SELECT ... FOR UPDATE`), verifies capacity, and updates status to `CONFIRMED`.
    - **Reject**: Transitions allocation to `REJECTED`.
    - **Cancel**: Releases capacity and sets status to `CANCELLED`.

---

### Screen 6: Utilization Dashboard (Date Window Evaluated)
- **File**: `docs/screenshots/06_Resource_utilization.png`
- **Navigation**: Click **"Utilization & Skill Gaps"** in the left sidebar.
- **Backing API**: `GET /api/analytics/reports/utilization-by-employee-department`
- **Expected UI Elements**:
  - Date window evaluation bar (`Window Start`, `Window End`, `Department`, and `Apply Window` button).
  - KPI metric cards: Active Headcount, Open Project Demands, Confirmed Allocations, and Avg Window Utilization (Peak vs Time-Weighted Avg).
  - Full-width Employee Capacity Utilization table showing peak simultaneous commitment and time-weighted averages across window.

---

### Screen 7: Enterprise Skill Shortage Analysis
- **File**: `docs/screenshots/07_Skill_gap_analysis.png`
- **Navigation**: Click **"Utilization & Skill Gaps"** in the left sidebar, then select **"Skill Gap Analysis"** sub-tab.
- **Backing API**: `GET /api/analytics/reports/skill-gaps-by-category`
- **Expected UI Elements**:
  - Net deficit summary cards: Categories with Deficit and Total Unmet Demand Slots.
  - Enterprise Skill Shortage table displaying Demanded Skill, Minimum Proficiency, Mandatory Count, Category, Demands Count, Qualified Supply, and Net Deficit status badges (`DEFICIT: -X` or `ADEQUATE`).

---

### Screen 8: AI Staffing Agent Assistant Panel
- **File**: `docs/screenshots/08_AI_staffing_agent_assistant.png`
- **Navigation**: Click **"AI Assistant"** in the left sidebar.
- **Backing API**: `POST /api/assistant/chat`, `GET /api/assistant/status`
- **Expected UI Elements**:
  - Header: AI Staffing Agent Assistant with `Read-Only Safety Enforced` badge.
  - Chat stream with message history, quick prompt chips, input area, and dynamic model execution indicator (`llama3.2:3b`).
  - Grounding evidence cards: Tool Invoked tag (e.g. `get_demand_candidates`, `check_employee_availability`), execution duration in milliseconds, and `Database Grounded` badge.
  - Tool allowlist sidebar showing all 6 registered read-only deterministic tools.

---

### Verification Artifact 9: Maven Automated Test Execution Evidence
- **File**: `docs/screenshots/Test_results.png`
- **Execution Command**: `.\mvnw.cmd test`
- **Expected Evidence Elements**:
  - Full test execution terminal output.
  - Final summary: `Tests run: 223, Failures: 0, Errors: 0, Skipped: 0`.
  - Overall status: `BUILD SUCCESS`.

