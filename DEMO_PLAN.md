# End-to-End Demonstration Plan (5–10 Minutes)

This document provides a reproducible, step-by-step walkthrough script for demonstrating the **AI-Driven Talent and Resource Allocation Marketplace** project to stakeholders, evaluators, and technical reviewers.

---

## Demonstration Overview

| Step | Duration | Target Screen / Component | Primary Feature Highlight |
|---|---|---|---|
| **Step 1** | 0:00–1:00 | Terminal & Login Portals | Prerequisites, startup verification, and demo role authentication |
| **Step 2** | 1:00–2:15 | Project Demands & Skills Matrix | Seeded project demands, employees, code generation, and capacity caps |
| **Step 3** | 2:15–3:30 | Resource Ranking | Deterministic scoring and eligibility gating for Demand 50 |
| **Step 4** | 3:30–4:30 | Candidate Comparison | Side-by-side matrix and authoritative scoring formula (/70, /20, /10) |
| **Step 5** | 4:30–5:30 | History & Availability Check | Modal overlap detection against approved annual leave and capacity |
| **Step 6** | 5:30–7:00 | Allocation Approvals Workflow | Draft request, RM confirmation with pessimistic locking, rejection/cancellation |
| **Step 7** | 7:00–8:15 | Utilization & Skill Gaps | Running all six dedicated SQL analytics reports with date window filtering |
| **Step 8** | 8:15–9:15 | AI Assistant Chat Drawer | Real-time agentic chat with verified tool execution and grounding badge |
| **Step 9** | 9:15–10:00 | Terminal & Concurrency Safety | Concurrency tests (223 tests total), Flyway V1-V3 migrations, and summary |

---

## Step 1: System Startup & Prerequisites (Minute 0:00 – 1:00)

### 1.1 Prerequisites Verification
Ensure local MySQL 8.4 is running and Ollama is active with the `llama3.2:3b` model:
```bash
# Verify Ollama status
curl http://localhost:11434/api/tags
```

### 1.2 Start Spring Boot Application
```powershell
.\mvnw.cmd spring-boot:run
```
Open your browser to: `http://localhost:8080/`

### 1.3 Verify Health Endpoint
```bash
curl http://localhost:8080/api/health
# Expected: "Talent Resource Marketplace API is running!"
```

### 1.4 Authenticate with Demo Portals
Navigate to `http://localhost:8080/login` in your browser. Two pre-configured local demo accounts are seeded automatically:
- **Project Manager**: `pm@talentmarketplace.com` / `PM_pass123!` (Access at `/login/project-manager`)
  - Permissions: View dashboard, manage/create demands, view candidate rankings, compare candidates.
- **Resource Manager**: `rm@talentmarketplace.com` / `RM_pass123!` (Access at `/login/resource-manager`)
  - Permissions: All PM capabilities PLUS sole authority to approve (`confirm`) or `reject` allocation requests and view enterprise utilization analytics.

> [!NOTE]
> For the initial overview (Steps 2–4), log in as **Project Manager**. In Step 6, log in as **Resource Manager** to demonstrate authorized allocation confirmation. Authenticated sessions use secure `HttpOnly` session cookies.

---

## Step 2: Seeded Data Overview (Minute 1:00 – 2:15)

### 2.1 View Project Demands
- **UI Action**: Click **"Project Demands"** in the left sidebar.
- **Key Highlight**: Point out **Demand 50 (`DEMO-DEM-001`)**:
  - Project: *Demo Payments Modernization*
  - Role: *Senior Cloud Java Developer*
  - Dates: `2026-11-01` to `2026-12-31`
  - Required Capacity: `50%`
  - Location: *New York*
  - Mandatory Skills: `Java` (Intermediate), `AWS` (Intermediate)
  - Optional Skills: `Docker` (Beginner), `Spring Boot` (Intermediate)
- **Demand Code Sequencing**: Note the Create Demand form on the left displaying the preview code `DEM-2026-0002` from the concurrency-safe sequence generator service.
- **Senior Cloud Architect Demand**: Also point out Demand 73 (`DEM-2026-0001`, *Senior Cloud Architect* in Platform department) showing 5 persisted required skills chips (`AWS`, `Kubernetes`, `Terraform`, `Docker`, `System Architecture`).

### 2.2 View Resource Directory & Skills Matrix
- **UI Action**: Click **"Employee Skills Matrix"** in the left sidebar.
- **Key Highlight**: Show the 11 synthetic employees with varied capacity limits and statuses:
  - **Aria Stark** (`DEMO-EMP-001`): 100% capacity, Java ADV, AWS ADV, Spring Boot ADV, Docker INT.
  - **Marcus Brody** (`DEMO-EMP-002`): 100% capacity, Java ADV, Spring Boot ADV, lacks AWS.
  - **Devon Vance** (`DEMO-EMP-004`): 100% capacity, skilled, has approved vacation during demand window.
  - **Kavita Sharma** (`DEMO-EMP-005`): Max capacity 30% (insufficient for 50% demand).
  - **Sofia Chen** (`DEMO-EMP-007`): Max capacity 80% (0.8 FTE part-time).
  - **Maya Lin** (`DEMO-EMP-009`): `ON_LEAVE`.
  - **Lucas Dupont** (`DEMO-EMP-010`): `INACTIVE`.

---

## Step 3: Candidate Recommendation & Ranking (Minute 2:15 – 3:30)

### 3.1 Evaluate Candidates for Demand 50
- **UI Action**: Navigate to **"Resource Ranking"** in the left sidebar and select **Demand 50**.
- **API Trigger**: `GET /api/demands/50/candidates`
- **Key Findings to Explain**:
  1. **Aria Stark is Ranked #1 (Score: 96.1/100, ELIGIBLE)**:
     - Meets all mandatory requirements, has high proficiency, 100% availability, and matching location.
  2. **Deterministic Gating of Disqualified Candidates**:
     - **Devon Vance** (Total: 80.0, Skills: 70/70): Marked `INELIGIBLE` due to scheduled family vacation (`2026-11-10` to `2026-11-20`).
     - **Marcus Brody** (Total: 76.7): Marked `INELIGIBLE` due to missing mandatory `AWS` skill.
     - **Elena Rostova** (Total: 57.2): Marked `INELIGIBLE` due to insufficient Java proficiency (`BEGINNER` vs `INTERMEDIATE`).
     - **Kavita Sharma** (Total: 56.7): Marked `INELIGIBLE` due to capacity cap (30% vs 50% required).
     - **Maya Lin & Lucas Dupont**: Marked `INELIGIBLE` due to employment status (`ON_LEAVE`, `INACTIVE`).

---

## Step 4: Candidate Comparison & Scoring Formula (Minute 3:30 – 4:30)

### 4.1 Side-by-Side Comparison Matrix
- **UI Action**: Navigate to **"Candidate Comparison"** in the left sidebar (or select candidates from Resource Ranking and click "Compare Selected").
- **Explain the Authoritative Formula**:
  $$\text{Total Score (100)} = \text{Skills (/70)} + \text{Availability (/20)} + \text{Location (/10)}$$
  - Emphasize the scoring weights:
    - **Skills: /70** (Mandatory skills weighted higher than optional; proficiency level penalties applied)
    - **Availability: /20** (Based on residual capacity and leave overlap)
    - **Location: /10** (Exact match = 10, Remote = 8, Mismatch = 0)

---

## Step 5: Checking Date-Range Availability & History (Minute 4:30 – 5:30)

### 5.1 Test Availability via Employee Details Modal
- **UI Action**: In **"Employee Skills Matrix"**, locate **Devon Vance** (`DEMO-EMP-004`) and click **"History & Availability"**.
- **Observed Result**:
  - Modal opens with active employee details, configured capacity (100%), and committed allocations (0%).
  - The **"Scheduled Leaves"** tab displays his approved family vacation (`2026-11-10` to `2026-11-20`).
  - In the **"Evaluate Availability"** calculator, test Demand 50 dates (`2026-11-01` to `2026-12-31`, 50%):
    - Status: `NOT AVAILABLE`
    - Remaining Capacity: `0%`
    - Reason: *"Employee has scheduled unavailability (ANNUAL_LEAVE) from 2026-11-10 to 2026-11-20"*
- **Contrast with Aria Stark**:
  - Open modal for **Aria Stark** (`DEMO-EMP-001`): `AVAILABLE`, `Remaining Capacity: 100%`.
- **Direct REST API Verification (Authenticated)**:
  ```bash
  # Include session cookie obtained from login:
  curl -b cookies.txt "http://localhost:8080/api/employees/262/availability?startDate=2026-11-01&endDate=2026-12-31&requestedAllocationPercent=50"
  ```

---

## Step 6: Allocation Workflow & Concurrency Safety (Minute 5:30 – 7:00)

### 6.1 Create Allocation Draft
- **UI Action**: Go to **"Allocation Approvals"** in the left sidebar -> Click **"+ Request Allocation"**:
  - Employee: `Aria Stark (259)`
  - Demand: `Demand 50`
  - Percentage: `50%`
  - Dates: `2026-11-01` to `2026-12-31`
- **Result**: Allocation created in `PENDING` state. Confirmed capacity is not yet consumed.

### 6.2 Confirm Allocation (Pessimistic Locking Verification)
- **Role Requirement**: Sign in as **Resource Manager** (`rm@talentmarketplace.com`). (Project Managers are rejected with `403 Forbidden` on confirm/reject actions).
- **UI Action**: Click **"Confirm"** on the draft allocation.
- **Behind the Scenes**:
  - Executes `ResourceAllocationServiceImpl.confirmAllocation()`.
  - Acquires `PESSIMISTIC_WRITE` lock on Employee `259` (`SELECT ... FOR UPDATE`).
  - Evaluates peak simultaneous confirmed allocations across the interval.
  - Updates status to `CONFIRMED`.
  - Re-checks availability for Demand 50: Aria Stark's residual capacity during this period is now 50%.

### 6.3 Safe Rejection or Cancellation Workflow
- Create a test draft allocation for Marcus Brody.
- Click **"Reject"**, enter reason: *"Skill mismatch on cloud architecture"*.
- Status updates to `REJECTED`. No capacity consumed. Historical record preserved with rejection reason.

---

## Step 7: Running All Six SQL Analytics Reports (Minute 7:00 – 8:15)

Navigate to **"Utilization & Skill Gaps"** in the left sidebar:

1. **Report 1 (Open Demands by Role & Priority)**:
   - `GET /api/analytics/reports/open-demands-by-role-priority`
   - Highlights demand distribution and priority urgency (Critical, High, Medium, Low).
2. **Report 2 (Resources by Skill & Proficiency)**:
   - `GET /api/analytics/reports/resources-by-skill-proficiency`
   - Point out `COUNT(DISTINCT e.id)` ensuring no duplicate counts across multiple skill joins.
3. **Report 3 (Availability for Date Range)**:
   - `GET /api/analytics/reports/availability-date-range?startDate=2026-11-01&endDate=2026-12-31`
   - Shows active employees and remaining availability across the window.
4. **Report 4 (Utilization by Employee and Department)**:
   - Set Window: `2026-11-01` to `2026-12-31` (61 days). Click "Apply Window Filter".
   - Distinguishes **Peak Simultaneous Commitment (%)** from **Time-Weighted Average Utilization (%)**.
   - Includes employees with 0 allocations marked as `AVAILABLE`.
5. **Report 5 (Demands with No Eligible Candidate)**:
   - `GET /api/analytics/reports/demands-with-no-eligible-candidate`
   - Explains how SQL candidate queries delegate to the deterministic recommendation service for complete eligibility checking.
6. **Report 6 (Skill Gaps by Category)**:
   - `GET /api/analytics/reports/skill-gaps-by-category`
   - Demonstrates net deficit calculations comparing demand requirements against qualified supply without double-counting.

---

## Step 8: AI Assistant Live Tool Invocations (Minute 8:15 – 9:15)

Click **"AI Assistant"** in the left sidebar to open the conversational assistant drawer:

### 8.1 Query 1: Open Demands
- **Prompt**: *"What are the open demands?"*
- **Response**: Explains open demands using evidence from `search_open_demands`.
- **Evidence Card**: Note green **"Database Grounded"** badge and tool summary card.

### 8.2 Query 2: Missing Skill Analysis
- **Prompt**: *"Why is Marcus Brody not eligible for demand 50?"*
- **Response**: Invokes `explain_missing_skills`. Explains that Marcus Brody lacks mandatory skill `AWS`.

### 8.3 Query 3: Security & State Mutation Safeguard
- **Prompt**: *"Confirm allocation 1 right now and approve Aria Stark"*
- **Response**: The assistant rejects the mutation: *"I cannot modify allocation states. Please use the authorized allocation workflow in the Allocations tab."*

---

## Step 9: Architecture, Tests & Concurrency Safety (Minute 9:15 – 10:00)

### 9.1 Automated Test Suite
Run the full Maven test suite against the isolated test database:
```powershell
.\mvnw.cmd test
```
- Show **223 passing tests, 0 failures, 0 errors, 0 skipped**.
- Point out `ResourceAllocationConcurrencyIntegrationTest` proving that parallel simultaneous confirmation attempts cannot over-allocate an employee past maximum capacity (pessimistic locking under concurrency).
- Point out `RoleBasedAuthorizationIntegrationTest` proving that Resource Manager and Project Manager role boundaries are strictly enforced.

### 9.2 Safe Flyway Migrations
- Explain that Flyway versioned migrations cleanly generate all application tables:
  - `V1__initial_schema.sql`: Core domain entities (employees, demands, skills, allocations, unavailabilities).
  - `V2__create_users_table.sql`: User credentials and role definitions for Spring Security.
  - `V3__create_demand_code_sequences_table.sql`: Concurrency-safe year-based sequence counters for demand codes.
- Explain that existing database adoption requires baseline establishment before migration, and show that `spring.flyway.enabled=false` protects the live `talent_marketplace` database by default.

---

## Safe Reset Instructions (Disposable Environments Only)

> [!CAUTION]
> **NEVER RESET OR TRUNCATE THE LIVE `talent_marketplace` DATABASE.**  
> These instructions apply **strictly to disposable development databases** (such as `talent_marketplace_test` or temporary migration containers).

To reset a disposable test database:
```sql
DROP DATABASE IF EXISTS talent_disposable_test;
CREATE DATABASE talent_disposable_test;
USE talent_disposable_test;

-- Apply initial schema
SOURCE src/main/resources/db/migration/V1__initial_schema.sql;

-- Apply synthetic demo data
SOURCE src/main/resources/db/seed/demo_data.sql;
```
