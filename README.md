# AI-Driven Talent and Resource Allocation Marketplace

An enterprise resource management application that matches employees to project demands using deterministic eligibility evaluation, multi-dimensional candidate scoring, real capacity/availability assessment, pessimistic locking concurrency control, safe Flyway migrations, six dedicated SQL analytics reports, and local Agentic AI with Ollama `llama3.2:3b`.

---

## 1. System Architecture & Tech Stack

- **Backend**: Java 25 / Spring Boot 4.1.1 (Spring Data JPA, Hibernate, Bean Validation, Spring WebMVC)
- **Database**: MySQL 8.4 Server
  - **Live Demo Database**: `talent_marketplace` (Protected; preserves seeded synthetic records)
  - **Isolated Test Database**: `talent_marketplace_test` (Targets all automated unit and integration tests)
- **Database Migrations**: Flyway Community (Versioned SQL migrations with `V1__initial_schema.sql` and baseline-on-migrate safety)
- **Concurrency Safety**: Pessimistic write locking (`PESSIMISTIC_WRITE` / `SELECT ... FOR UPDATE`) on Employee records during allocation confirmation to guarantee cumulative capacity integrity.
- **Local AI Agent**: Ollama 0.35.0 running `llama3.2:3b`
  - Zero external cloud LLM dependencies or paid API keys.
  - Controlled typed tool allowlist backed by existing deterministic Java services.
  - Grounding verification: Responses marked database-grounded only when tools succeed and return usable business data.
- **Frontend**: Vanilla HTML5, CSS3, and JavaScript Single Page Application served directly from Spring Boot static resources (`http://localhost:8080/`).

---

## 2. Seven Interactive UI Screens

The application UI serves 7 distinct operational screens:
1. **Demand Intake / Management**: View, filter, and create project demands, skill requirements, and priorities.
2. **Resource Directory & Skills Matrix**: Search employees, view statuses (`ACTIVE`, `ON_LEAVE`, `INACTIVE`), department, location, and verified skill proficiencies.
3. **Candidate Recommendations & Scoring**: Deterministic candidate rankings for selected demands (e.g., Demand 50) using the authoritative formula: **Skills: /70**, **Availability: /20**, **Location: /10** (**Total: /100**).
4. **Candidate Side-by-Side Comparison**: Multi-candidate comparative grid highlighting skill matching, proficiency deltas, and scoring breakdowns.
5. **Allocation Workflow Management**: Draft allocation creation (`PENDING`), pessimistic locking confirmation (`CONFIRMED`), rejection with reasons, and cancellation.
6. **Utilization & Analytics Dashboard**: Date-window evaluated resource utilization metrics with discrete evaluation windows `[startDate, endDate]`, distinguishing **Peak Simultaneous Commitment (%)** from **Time-Weighted Average Utilization (%)**.
7. **Agentic AI Assistant Chat**: Interactive conversational drawer with natural language query answering, tool execution summaries, and verified database-grounding badges.

---

## 3. Six Dedicated SQL Analytics Reports

The reporting engine exposes dedicated REST endpoints backed by native MySQL queries (`src/main/resources/db/reports/`):

| Report # | Report Name | Endpoint | SQL Script File | Description |
|---|---|---|---|---|
| **Report 1** | Open Demands by Role & Priority | `GET /api/analytics/reports/open-demands-by-role-priority` | `src/main/resources/db/reports/report1_open_demands.sql` | Aggregates open demands grouped by role and priority urgency. |
| **Report 2** | Resources by Skill & Proficiency | `GET /api/analytics/reports/resources-by-skill-proficiency` | `src/main/resources/db/reports/report2_resources_by_skill.sql` | Active employee counts per skill and level using `COUNT(DISTINCT e.id)`. |
| **Report 3** | Availability for Date Range | `GET /api/analytics/reports/availability-date-range` | `src/main/resources/db/reports/report3_availability_date_range.sql` | Availability sweep over `[startDate, endDate]` accounting for leave and confirmed allocations. |
| **Report 4** | Utilization by Employee & Department | `GET /api/analytics/reports/utilization-by-employee-department` | `src/main/resources/db/reports/report4_utilization_by_employee_dept.sql` | Time-weighted average utilization and peak simultaneous commitment over window; includes zero-allocation employees. |
| **Report 5** | Demands with No Eligible Candidate | `GET /api/analytics/reports/demands-with-no-eligible-candidate` | `src/main/resources/db/reports/report5_demands_no_eligible_candidate.sql` | Identifies open demands with zero eligible candidates via deterministic recommendation engine. |
| **Report 6** | Skill Gaps by Category | `GET /api/analytics/reports/skill-gaps-by-category` | `src/main/resources/db/reports/report6_skill_gaps_by_category.sql` | Aggregates demand requirements vs. qualified employee supply per category without double-counting. |

---

## 4. Flyway Migrations & Database Setup

### Clean Database Initialization
For fresh installations or clean testing containers:
1. Apply versioned schema migration:
   ```bash
   src/main/resources/db/migration/V1__initial_schema.sql
   ```
2. Apply synthetic demo seed data:
   ```bash
   src/main/resources/db/seed/demo_data.sql
   ```
3. Hibernate schema validation (`spring.jpa.hibernate.ddl-auto=validate`) confirms 100% equivalence between the Flyway schema and JPA entities.

### Safe Existing Database Adoption (`talent_marketplace`)
Existing production/demo databases that were created without Flyway metadata must not have `flyway.migrate()` run without a baseline.
- **Safety Precondition**: `spring.flyway.enabled=false` is configured in `application.properties` to ensure automated application runs never alter `talent_marketplace`.
- **Manual Adoption Procedure**:
  ```powershell
  # Establish baseline at version 1 without executing DDL:
  flyway -url="jdbc:mysql://localhost:3306/talent_marketplace?useSSL=false&allowPublicKeyRetrieval=true" `
         -user="root" -password="rootpassword" `
         -baselineVersion="1" -baselineDescription="Initial Schema" baseline
  ```

---

## 5. Local Ollama AI Setup

### Prerequisites
1. Download and install [Ollama](https://ollama.com/) (version 0.35.0 or later).
2. Start the local daemon:
   ```bash
   ollama serve
   ```
3. Pull the required model:
   ```bash
   ollama pull llama3.2:3b
   ```
4. Verify local connectivity:
   ```bash
   curl http://localhost:11434/api/tags
   ```

### Six Allowlisted Agentic Tools
The AI Assistant interacts with application state exclusively through an allowlist of 6 typed Java service tools:
- `search_open_demands`: Search open demands with role/location filters.
- `get_demand_candidates`: Retrieve deterministic candidate rankings and scores for a demand.
- `check_employee_availability`: Check candidate availability against leave and confirmed allocations.
- `explain_missing_skills`: Explain mandatory and optional skill deficits for a candidate.
- `compare_candidates`: Side-by-side comparative analysis of top candidates.
- `get_allocation_status`: Retrieve current confirmed and pending allocation records.

---

## 6. Application Startup & Verification

### Start Application
```powershell
.\mvnw.cmd spring-boot:run
```
The server will start on port `8080`. Access the UI at `http://localhost:8080/`.

### Run Automated Tests
```powershell
.\mvnw.cmd clean test
```
The test suite runs **159 automated tests (0 failures, 0 errors, 0 skipped)** strictly against `talent_marketplace_test`.

---

## 7. Deliverables & Documentation Index

- **Postman API Collection**: [docs/postman/talent_resource_marketplace.postman_collection.json](file:///c:/Users/SHemalatha/Downloads/talent-resource-marketplace/docs/postman/talent_resource_marketplace.postman_collection.json) (53 requests covering Phases 1–9).
- **Screenshot Verification Guide**: [docs/SCREENSHOTS.md](file:///c:/Users/SHemalatha/Downloads/talent-resource-marketplace/docs/SCREENSHOTS.md) (Step-by-step manual capture guide for all 9 screens).
- **AI Usage & Integration Log**: [AI_USAGE_LOG.md](file:///c:/Users/SHemalatha/Downloads/talent-resource-marketplace/AI_USAGE_LOG.md) (Architecture, 6 tools, grounding criteria, and verified experiments).
- **Demonstration Plan**: [DEMO_PLAN.md](file:///c:/Users/SHemalatha/Downloads/talent-resource-marketplace/DEMO_PLAN.md) (Reproducible 5–10 minute demonstration script with minute markers).
- **Submission Checklist**: [docs/SUBMISSION_CHECKLIST.md](file:///c:/Users/SHemalatha/Downloads/talent-resource-marketplace/docs/SUBMISSION_CHECKLIST.md) (Full mapping of requirements and TC-06-01 through TC-06-12).

---

## 8. Database Safety & Test Isolation

To safeguard live demo records, automated tests are programmatically isolated from `talent_marketplace`:
1. **Dedicated Test Database**: `talent_marketplace_test` (`src/test/resources/application.properties`).
2. **Programmatic Safety Guard**: `TestDatabaseSafetyGuard` inspects the active JDBC connection catalog on context initialization and before every `@BeforeEach cleanDatabase()`. If connected to `talent_marketplace`, tests abort immediately with an `IllegalStateException`.
3. **Demo Table Integrity**: Table counts on `talent_marketplace` are monitored before and after testing to ensure 0 records are ever dropped, truncated, or modified.
