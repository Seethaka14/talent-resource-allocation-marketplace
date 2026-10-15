# AI Usage and Agentic Integration Log

This document records the architectural design, configuration, tool allowlisting, database-grounding criteria, safety enforcement, verified experiments, and operational limitations of the local AI Assistant in the **AI-Driven Talent and Resource Allocation Marketplace**.

---

## 1. Model & Local Ollama Environment

- **Model**: `llama3.2:3b` (3.2 Billion parameters, 4-bit quantized `Q4_K_M`, GGUF format, context length 131,072 tokens).
- **Inference Server**: Local [Ollama](https://ollama.com/) daemon (v0.35.0).
- **Server Endpoint**: `http://localhost:11434`
- **Application Configuration Properties** (`application.properties`):
  ```properties
  ollama.base-url=http://localhost:11434
  ollama.model=llama3.2:3b
  ollama.timeout-seconds=120
  ```
- **External Dependencies**: Zero external cloud API calls, zero commercial API keys, and zero external network transmissions. All inference executes locally on the host machine.
- **Verification Status**:
  - Live tags endpoint verified: `GET http://localhost:11434/api/tags` returned `llama3.2:3b` (size: 2,019,393,189 bytes, digest: `a80c4f17...`).
  - Model capabilities: `completion tools`.

---

## 2. Six Allowlisted Agentic Tools

The AI Assistant is strictly decoupled from direct database access. It cannot generate or execute arbitrary SQL, JPA criteria queries, or system code. Instead, it interacts with application state exclusively through an explicit allowlist of six typed Java service tools registered in `AiToolRegistry`:

| Tool Identifier | Backing Java Service | Input Parameters | Return Type | Business Purpose |
|---|---|---|---|---|
| `search_open_demands` | `ProjectDemandService` | `department` (str), `role` (str), `location` (str) | `List<DemandResponse>` | Searches open project demands, required dates, capacity requirements, and skill tags. |
| `get_demand_candidates` | `CandidateRecommendationService` | `demandId` (long, required) | `DemandRecommendationListResponse` | Retrieves deterministic candidate rankings, scores, and eligibility status. |
| `check_employee_availability` | `AvailabilityService` | `employeeId` (or name), `startDate`, `endDate`, `allocationPercent` | `AvailabilityAssessmentResponse` | Evaluates candidate availability against confirmed allocations and scheduled leave. |
| `explain_missing_skills` | `CandidateRecommendationService` | `demandId` (long, required), `employeeId` (str/long) | `MissingSkillsExplanationResponse` | Identifies missing mandatory skills, missing optional skills, and proficiency shortfalls. |
| `compare_candidates` | `CandidateRecommendationService` | `demandId` (long, required), `limit` (int, default: 3) | `CandidateComparisonResponse` | Side-by-side comparative scoring breakdown of top-ranking candidates. |
| `get_allocation_status` | `ResourceAllocationService` | `demandId` (long), `employeeId` (long) | `List<AllocationResponse>` | Retrieves active confirmed and pending allocation records. |

### Tool Selection & Execution Architecture
1. **Dual-Mode Dispatch**:
   - Primary: Native Ollama OpenAI-compatible tool calling protocol (`tools` array sent in chat completion payload).
   - Fallback: Validated structured JSON extraction from model response if the local model formats tool calls within standard content blocks.
2. **Allowlist Enforcement**:
   - `AiToolRegistry` validates every requested tool against its immutable registry map.
   - Any unregistered or unauthorized tool name throws `SecurityException` and immediately halts execution.
3. **Execution Guardrails**:
   - Maximum loop limit of **3 tool iterations** prevents infinite recursive tool calling.
   - Input arguments are deserialized into typed Java request DTOs and validated with Bean Validation before invoking underlying services.

---

## 3. Strict Read-Only Policy & Allocation State Safeguards

- **Principle of Least Privilege**: The AI Assistant operates in a **strictly read-only** capacity.
- **State Mutation Prevention**:
  - No tool exists in `AiToolRegistry` that can confirm, reject, create, or cancel an allocation.
  - Allocation creation and confirmation are reserved exclusively for authorized user workflows via `POST /api/allocations` and `POST /api/allocations/{id}/confirm` (protected by Phase 7 pessimistic locking).
- **Proactive Intent Interception**:
  - If a user prompt contains state modification directives (e.g., *"Confirm allocation for Aria Stark"*, *"Approve Marcus Brody"*, *"Change demand status to closed"*), the assistant intercepts the intent and returns a security notice:
    > *"I cannot modify allocation states. Please use the authorized allocation workflow in the Allocations tab."*

---

## 4. Database-Grounding Criteria (Phase 9A Refinement)

Under Phase 9A, the grounding decision logic in `AssistantServiceImpl` was enhanced to eliminate false-positive grounding flags:

### Previous Vulnerability (Pre-9A)
- Grounding was marked `true` whenever any tool was invoked, even if the tool threw an exception or returned an empty payload.

### Authoritative Phase 9A Grounding Decision
A response is marked `grounded: true` **only if**:
1. At least one tool was invoked.
2. The tool execution succeeded without errors (`executionSummary.isSuccess() == true`).
3. The tool returned usable evidence (`executionSummary.isUsableEvidence() == true`), defined as:
   - Non-null payload.
   - Non-empty collection or non-empty map.
   - For recommendation responses: contains at least one candidate record.

```java
// Authoritative check in AssistantServiceImpl.java
boolean grounded = !toolsInvoked.isEmpty() && 
    toolsInvoked.stream().anyMatch(t -> t.isSuccess() && t.isUsableEvidence());
```

### Truthfulness Guarantee Scope
The `grounded` flag signifies that factual, authoritative database records were retrieved and supplied into the model's context window. It does not mathematically guarantee that every conversational word generated by a generative LLM is semantically flawless, but it prevents ungrounded hallucinations from being badged as verified data.

---

## 5. Division: Deterministic Backend Logic vs. AI Explanations

To ensure compliance with enterprise resource allocation safety standards, all mathematical, eligibility, and concurrency logic is strictly confined to deterministic Java code:

| Component | Handled by Java | Handled by LLM |
|---|---|---|
| **Candidate Scoring** | 100% Deterministic (Skills /70, Availability /20, Location /10) | Explains score breakdown in natural language |
| **Eligibility Determination** | 100% Deterministic (Mandatory skills, proficiency thresholds, leave overlap, capacity limit) | Summarizes exclusion reasons (e.g. missing AWS) |
| **Availability Assessment** | 100% Deterministic (Overlap sweep against confirmed allocations and approved leave) | Interprets availability status for user |
| **Capacity & Allocation Safety** | 100% Deterministic (Pessimistic write locks, cumulative capacity validation) | Not accessible to LLM (strictly read-only) |
| **SQL Analytics Reports** | 100% Deterministic (Native MySQL queries and timeline calculations) | Summarizes report findings on user request |

---

## 6. Actual Tests and Verified Experiments

### 1. Automated Unit Tests (`AssistantServiceTest.java`)
- `chat_SuccessfulToolExecution_GroundedTrue`: Verified that successful tool execution returning non-empty candidate data sets `grounded = true`.
- `chat_FailedToolInvocation_NotGrounded`: Verified that a tool throwing an exception sets `grounded = false`.
- `chat_EmptyToolResults_NotGrounded`: Verified that a tool returning an empty list sets `grounded = false`.
- `chat_SecurityRejection_StateModification`: Verified that requests attempting allocation confirmation are blocked without tool invocation.

### 2. Live Verification Experiment (Recorded Task-2129)
- **Prompt**: *"What are the open demands?"*
- **Request**: `POST http://localhost:8080/api/assistant/chat`
- **Live Response Received**:
  ```json
  {
    "success": true,
    "message": "Assistant response generated successfully",
    "data": {
      "answer": "Based on the current data available, the open demands are:\n\n1. Project Lead for a 6-month contract in New York City...\n2. Data Scientist...\n3. UX Designer...",
      "toolsInvoked": [
        {
          "toolName": "search_open_demands",
          "arguments": "@{location=; role=; department=}",
          "executionDurationMs": 32,
          "success": true,
          "errorMessage": null,
          "usableEvidence": true
        }
      ],
      "liveModelUsed": true,
      "modelName": "llama3.2:3b",
      "grounded": true,
      "timestamp": "2026-10-15T10:27:39.3438941"
    }
  }
  ```

---

## 7. Known Limitations and Operational Risks

1. **Local Model Capacity**: `llama3.2:3b` is an efficient 3.2B parameter model. While it reliably triggers structured tools, its conversational responses may occasionally paraphrase role titles or add polite conversational framing.
2. **Inference Latency**: Depending on host CPU/GPU utilization, response generation latency ranges from 800ms to 25 seconds. The application configures a 120-second timeout to handle peak host load.
3. **Graceful Offline Degradation**: If the local Ollama daemon is not running, the application gracefully handles `ResourceAccessException`, falls back to deterministic rule explanations where possible, and explicitly marks `liveModelUsed: false` and `grounded: false`.
