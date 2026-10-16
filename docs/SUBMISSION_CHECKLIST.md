# Assignment Requirements & Test Case Submission Checklist

This document maps all original project requirements, architectural components, user interface screens, SQL reports, and the 12 mandatory test scenarios (TC-06-01 through TC-06-12) to their authoritative implementations, test classes, and documentation evidence.

---

## 1. Core Architecture & Technology Stack Checklist

| Requirement | Specification | Implementation File(s) | Verification Status | Evidence / Test Class |
|---|---|---|:---:|---|
| **Java Version** | Java 17 or higher | `pom.xml` (Java 25 active) | **Verified** | Maven build passing on JDK 25 |
| **Framework** | Spring Boot | `pom.xml` (Spring Boot 4.1.1) | **Verified** | `TalentMarketplaceApplication.java` |
| **Persistence** | Spring Data JPA / Hibernate | `src/main/java/com/talentmarketplace/repository/` | **Verified** | Repositories with custom JPA & Native SQL |
| **Database** | MySQL 8+ | MySQL 8.4 Server (`talent_marketplace`, `talent_marketplace_test`) | **Verified** | `application.properties`, `schema.sql`, Flyway |
| **Frontend** | Vanilla HTML5, CSS3, JS | `src/main/resources/static/` (`index.html`, `style.css`, `app.js`) | **Verified** | 7 functional interactive screens |
| **Testing** | JUnit 5 & Mockito | `src/test/java/com/talentmarketplace/` | **Verified** | 223 tests passing, 0 failures, 0 errors, 0 skipped |
| **Local AI Agent** | Local Ollama `llama3.2:3b` | `com.talentmarketplace.ai.*`, `AssistantServiceImpl.java` | **Verified** | Live Ollama tag & chat tests (Task-2129) |
| **Migrations** | Versioned Flyway Migrations | `src/main/resources/db/migration/` (V1, V2, V3) | **Verified** | `FlywayMigrationIntegrationTest.java` (V1-V3 verified) |
| **Demo Dataset** | Repeatable & Idempotent | `src/main/resources/db/seed/demo_data.sql` | **Verified** | 11 employees, 14 skills, 5 demands seeded |

---

## 2. Mandatory Test Cases (TC-06-01 through TC-06-12)

| Test ID | Test Scenario Description | Implementation File(s) | Test Class & Method | Assertion & Verification | Status |
|---|---|---|---|---|:---:|
| **TC-06-01** | **Valid Demand Creation**: Demand created with valid attributes, dates, capacity, and mandatory skills. | `ProjectDemandServiceImpl.java`, `ProjectDemandController.java` | `ProjectDemandControllerIntegrationTest.java`<br>`tc06_01_createDemand_Success()` | Asserts HTTP 201 Created, assigned database ID, status `OPEN`, and persisted mandatory skill requirements. | **PASS** |
| **TC-06-02** | **Invalid Demand Creation**: Creation rejected for missing required fields or inverted dates (start after end). | `CreateDemandRequest.java`, `ProjectDemandServiceImpl.java` | `ProjectDemandControllerIntegrationTest.java`<br>`tc06_02_createDemand_InvalidDatesOrRole_Rejected()` | Asserts HTTP 400 Bad Request and validation error messages. | **PASS** |
| **TC-06-03** | **Candidate Ranking**: Deterministic candidate ranking based on multi-dimensional scoring formula with tie-breaking. | `CandidateRecommendationServiceImpl.java` | `CandidateRecommendationServiceTest.java`<br>`testRecommend_deterministicRankingAndTieBreaking()` | Verifies sorting by total score descending (Aria Stark #1 with 96.1/100) and tie-breaking by employee ID. | **PASS** |
| **TC-06-04** | **Missing Mandatory Skill**: Candidate missing any mandatory skill or proficiency threshold marked ineligible. | `CandidateRecommendationServiceImpl.java` | `CandidateRecommendationServiceTest.java`<br>`testRecommend_mandatorySkillMissing()` | Verifies Marcus Brody disqualified due to missing mandatory `AWS` skill (`eligible = false`). | **PASS** |
| **TC-06-05** | **Allocated Date Ranges**: Availability engine calculates remaining capacity across partial and full overlapping assignments. | `AvailabilityServiceImpl.java`, `ResourceAllocationRepository.java` | `AvailabilityServiceTest.java`<br>`testAssessAvailability_confirmedAllocationConsumesCapacity()` | Confirmed allocations reduce residual capacity; non-overlapping allocations do not consume capacity. | **PASS** |
| **TC-06-06** | **Allocation Confirmation**: Transitions allocation from `PENDING` to `CONFIRMED` with audit timestamps. | `ResourceAllocationServiceImpl.java` | `ResourceAllocationServiceTest.java`<br>`testConfirmAllocation_success_updatesDemandStatus()` | Verifies state changes to `CONFIRMED`, `confirmedBy` and `confirmedAt` are recorded, and capacity is locked. | **PASS** |
| **TC-06-07** | **Overcapacity Prevention**: Rejects allocation confirmation when peak simultaneous commitment exceeds maximum capacity. | `ResourceAllocationServiceImpl.java` | `ResourceAllocationServiceTest.java`<br>`testConfirmAllocation_capacityRevalidationFails()` | Throws `BusinessRuleValidationException` and returns HTTP 400 when cumulative commitment exceeds max capacity limit. | **PASS** |
| **TC-06-08** | **AI Evidence Grounding**: Responses marked `grounded: true` only when tools succeed and return usable database records. | `AssistantServiceImpl.java`, `AiToolRegistry.java` | `AssistantServiceTest.java`<br>`chat_SuccessfulToolInvocationLoop()`, `chat_FailedToolInvocation_NotGrounded()` | Verifies grounding requires `isSuccess() && isUsableEvidence()`. Failed or empty tool responses set `grounded = false`. | **PASS** |
| **TC-06-09** | **Missing Data Handling**: Graceful error handling for missing entities, unmapped skills, or offline model services. | `GlobalExceptionHandler.java`, `AssistantServiceImpl.java` | `AssistantServiceTest.java`<br>`chat_OllamaUnavailable_ThrowsException()`, `GlobalExceptionHandlerTest.java` | Returns HTTP 404 for missing entities and graceful fallback responses when Ollama daemon is offline. | **PASS** |
| **TC-06-10** | **Allocation Concurrency Safety**: Parallel simultaneous confirmations for overlapping dates prevent race conditions. | `ResourceAllocationServiceImpl.java` (`PESSIMISTIC_WRITE`) | `ResourceAllocationConcurrencyIntegrationTest.java`<br>`testSimultaneousConfirmations_exceedingCapacity_exactlyOneSucceeds()` | Two concurrent threads attempt 60% allocation on 100% capacity employee: exactly 1 succeeds, 1 rejected. | **PASS** |
| **TC-06-11** | **Allocation Cancellation**: Cancelling a confirmed allocation immediately restores employee available capacity. | `ResourceAllocationServiceImpl.java` | `ResourceAllocationServiceTest.java`<br>`testCancelAllocation_success()`, `testCancelAllocation_preservesHistoryRecord()` | Verifies status transitions to `CANCELLED` and subsequent availability checks reflect restored capacity. | **PASS** |
| **TC-06-12** | **Unavailability Intersection**: Approved annual leave or training periods intersect demand dates and block availability. | `AvailabilityServiceImpl.java`, `EmployeeUnavailabilityServiceImpl.java` | `AvailabilityServiceTest.java`<br>`testAssessAvailability_unavailabilityOverlappingDemandPeriod()` | Devon Vance disqualified from Demand 50 due to overlapping family vacation (`2026-11-10` to `2026-11-20`). | **PASS** |

---

## 3. Seven Required User Interface Screens Checklist

| Screen | Target Functionality | Backing API | Implementation File(s) | UI Verification Status |
|---|---|---|---|:---:|
| **Screen 1** | Demand Intake & Management | `GET /api/demands` | `index.html` (`#screen-demand-intake`), `app.js` (`loadOpenDemandsList()`) | **Verified** |
| **Screen 2** | Resource Directory & Skills Matrix | `GET /api/employees`, `GET /api/skills` | `index.html` (`#screen-skills-matrix`), `app.js` (`loadSkillsMatrix()`) | **Verified** |
| **Screen 3** | Candidate Recommendations & Scoring | `GET /api/demands/{id}/candidates` | `index.html` (`#screen-resource-ranking`), `app.js` (`loadCandidatesForDemand()`) | **Verified** |
| **Screen 4** | Candidate Side-by-Side Comparison | `GET /api/demands/{id}/candidates` | `index.html` (`#screen-candidate-comparison`), `app.js` (`renderComparisonMatrix()`) | **Verified** |
| **Screen 5** | Allocation Management & Approval | `POST /api/allocations`, `POST /api/allocations/{id}/confirm` | `index.html` (`#screen-allocation-approvals`), `app.js` (`loadAllocations()`) | **Verified** |
| **Screen 6** | Utilization & Analytics Dashboard | `GET /api/analytics/reports/utilization-by-employee-department` | `index.html` (`#screen-utilization-dashboard`), `app.js` (`loadUtilizationDashboard()`) | **Verified** |
| **Screen 7** | Agentic AI Assistant Chat Panel | `POST /api/assistant/chat`, `GET /api/assistant/status` | `index.html` (`#screen-ai-assistant`), `app.js` (`sendChatMessage()`) | **Verified** |

---

## 4. Six Dedicated SQL Analytics Reports Checklist

| Report # | Report Title | Dedicated Endpoint | SQL Script File | Projection / Repository Method | Status |
|---|---|---|---|---|:---:|
| **Report 1** | Open Demands by Role & Priority | `GET /api/analytics/reports/open-demands-by-role-priority` | `src/main/resources/db/reports/report1_open_demands.sql` | `AnalyticsRepository.findOpenDemandsByRoleAndPriority()` | **Verified** |
| **Report 2** | Resources by Skill & Proficiency | `GET /api/analytics/reports/resources-by-skill-proficiency` | `src/main/resources/db/reports/report2_resources_by_skill.sql` | `AnalyticsRepository.findActiveResourcesBySkillAndProficiency()` | **Verified** |
| **Report 3** | Availability for Date Range | `GET /api/analytics/reports/availability-date-range` | `src/main/resources/db/reports/report3_availability_date_range.sql` | `AnalyticsRepository.findActiveEmployeesByDepartment()` + Overlap Sweep | **Verified** |
| **Report 4** | Utilization by Employee & Department | `GET /api/analytics/reports/utilization-by-employee-department` | `src/main/resources/db/reports/report4_utilization_by_employee_dept.sql` | `ResourceAllocationRepository.findAllOverlappingConfirmedAllocations()` | **Verified** |
| **Report 5** | Demands with No Eligible Candidate | `GET /api/analytics/reports/demands-with-no-eligible-candidate` | `src/main/resources/db/reports/report5_demands_no_eligible_candidate.sql` | `AnalyticsRepository.findCandidateOpenDemands()` + `CandidateRecommendationService` | **Verified** |
| **Report 6** | Skill Gaps by Category | `GET /api/analytics/reports/skill-gaps-by-category` | `src/main/resources/db/reports/report6_skill_gaps_by_category.sql` | `AnalyticsRepository.findOpenDemandSkillRequirements()` + Supply Sweep | **Verified** |

---

## 5. Submission Deliverables Summary

- [x] **Postman Collection**: `docs/postman/talent_resource_marketplace.postman_collection.json` (59 requests across 10 folders).
- [x] **Screenshot Documentation**: `docs/SCREENSHOTS.md` (Covers all 9 screens with manual verification guides).
- [x] **AI Usage Log**: `AI_USAGE_LOG.md` (Documents model, 6 tools, allowlist security, grounding logic, and limitations).
- [x] **Demonstration Plan**: `DEMO_PLAN.md` (Reproducible 5–10 minute script with minute markers and API calls).
- [x] **Checklist Mapping**: `docs/SUBMISSION_CHECKLIST.md` (Maps all requirements and TC-06-01 through TC-06-12).
- [x] **Updated README**: `README.md` (Reflects environment, APIs, reports, Flyway, and startup).
