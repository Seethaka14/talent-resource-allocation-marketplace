# AI-Driven Talent and Resource Allocation Marketplace

An enterprise resource management application that matches employees to project demands using deterministic eligibility evaluation, multi-dimensional candidate scoring, real capacity/availability assessment, and local Agentic AI with Ollama `llama3.2:3b`.

---

## 1. System Architecture & Tech Stack

- **Backend**: Java 25 / Spring Boot 4.1.1 (Spring Data JPA, Hibernate, Bean Validation, Spring WebMVC)
- **Database**: MySQL 8.4
  - **Live Demo Database**: `talent_marketplace` (Protected; preserves seeded synthetic records)
  - **Isolated Test Database**: `talent_marketplace_test` (Targets all automated unit and integration tests)
- **Local AI Agent**: Ollama 0.35.0 running `llama3.2:3b`
  - No external cloud LLM dependencies or paid API keys required.
  - Controlled typed tool allowlist backed by existing deterministic Java services.
  - Hybrid tool calling: Native Ollama OpenAI-compatible tool calling with structured-JSON fallback.
- **Frontend**: Clean Vanilla HTML5, CSS3, and JavaScript Single Page Interface served directly from Spring Boot (`src/main/resources/static/index.html`).

---

## 2. Ollama Setup & Model Verification

### Prerequisites
1. Download and install [Ollama](https://ollama.com/) (version 0.35.0 or later).
2. Start the Ollama local daemon:
   ```bash
   ollama serve
   ```
3. Pull or run the required `llama3.2:3b` model:
   ```bash
   ollama run llama3.2:3b
   ```

### Verifying Ollama Connectivity and Model
Test the local Ollama tags endpoint:
```bash
curl http://localhost:11434/api/tags
```
Ensure `llama3.2:3b` appears in the list of installed models:
```json
{
  "models": [
    {
      "name": "llama3.2:3b",
      "model": "llama3.2:3b",
      "details": {
        "parameter_size": "3.2B",
        "quantization_level": "Q4_K_M"
      }
    }
  ]
}
```

---

## 3. Configuration & Environment Variables

The application configures sensible local defaults and can be overridden via environment variables:

| Environment Variable | Property Key | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `OLLAMA_BASE_URL` | `ollama.base-url` | `http://localhost:11434` | Base URL of local Ollama HTTP server |
| `OLLAMA_MODEL` | `ollama.model` | `llama3.2:3b` | Local model name to use for AI Assistant |
| `OLLAMA_TIMEOUT_SECONDS` | `ollama.timeout-seconds` | `120` | Request timeout for local CPU/GPU generation |
| `SPRING_DATASOURCE_URL` | `spring.datasource.url` | `jdbc:mysql://localhost:3306/talent_marketplace` | Live application database |

---

## 4. Running the Application

### Start Backend Application
```powershell
.\mvnw.cmd spring-boot:run
```
The server will start on port `8080`.

### Access the Web UI
Open your browser at:
```
http://localhost:8080/
```
The UI provides an interactive AI Assistant chat panel on the left and a live Marketplace Overview dashboard on the right with quick prompt suggestions, grounding badges, and tool execution metrics.

---

## 5. Controlled Agentic AI Tools (Allowlist)

To ensure zero hallucination and strict database grounding, the AI Assistant cannot execute arbitrary SQL or code. It accesses real application data strictly through an explicit allowlist of 6 typed Java service tools:

| Tool Name | Backing Service | Parameters | Description |
| :--- | :--- | :--- | :--- |
| `search_open_demands` | `ProjectDemandService` | `department`, `role`, `location` | Search open project demands with required skills |
| `get_demand_candidates` | `CandidateRecommendationService` | `demandId` (required) | Retrieve deterministic candidate rankings, scores, and match explanations |
| `check_employee_availability` | `AvailabilityService` | `employeeId` (or name), `startDate`, `endDate`, `allocationPercent` | Check availability over date range against leave and active allocations |
| `explain_missing_skills` | `CandidateRecommendationService` | `demandId` (required), `employeeId` (optional) | Explain mandatory skill gaps, optional skill gaps, and proficiency differences |
| `compare_candidates` | `CandidateRecommendationService` | `demandId` (required), `limit` (default: 3) | Side-by-side comparative analysis of top candidates |
| `get_allocation_status` | `ResourceAllocationService` | `demandId`, `employeeId` | Retrieve current confirmed and pending allocation records |

### Tool Selection Architecture
- **Dual-Mode Execution**:
  1. Native tool calling: Sends tool definitions in the Ollama `tools` array.
  2. Structured JSON fallback: Parses valid JSON tool calls in the model's text if `tool_calls` is empty.
- **Safety Enforcement**:
  - `AiToolRegistry` rejects any unapproved tool with a `SecurityException`.
  - Max loop limit of 3 iterations prevents unbounded execution.
  - Arguments are validated in Java before service invocation.
- **Allocation Safety**:
  - The AI Assistant operates in **strictly read-only** mode.
  - Any prompt requesting state changes (e.g., "confirm allocation", "approve Marcus Brody") is intercepted and rejected with instructions to use the authorized allocation workflow (`POST /api/allocations/{id}/confirm`).

---

## 6. Example API Requests & Responses

### Check AI Assistant Status
**Request**:
```http
GET /api/assistant/status HTTP/1.1
Host: localhost:8080
```
**Response**:
```json
{
  "success": true,
  "message": "AI Assistant status",
  "data": {
    "ollamaReachable": true,
    "model": "llama3.2:3b",
    "toolsCount": 6,
    "allowedTools": [
      "get_demand_candidates",
      "search_open_demands",
      "compare_candidates",
      "explain_missing_skills",
      "get_allocation_status",
      "check_employee_availability"
    ]
  },
  "timestamp": "2026-10-14T20:08:36"
}
```

### Natural Language Question (Skill Gap Analysis)
**Request**:
```http
POST /api/assistant/chat HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "message": "Why is Marcus Brody not eligible for demand 50?"
}
```
**Response**:
```json
{
  "success": true,
  "message": "Assistant response generated successfully",
  "data": {
    "answer": "Marcus Brody is not eligible for demand 50 because he is missing the mandatory skill of AWS (Amazon Web Services). The system requires this skill for the Senior Java Backend Engineer role, and since Marcus Brody does not possess it, he is not eligible for this demand.",
    "demandId": 50,
    "employeeId": null,
    "toolsInvoked": [
      {
        "toolName": "explain_missing_skills",
        "arguments": {
          "demandId": 50,
          "employeeId": "Marcus Brody"
        },
        "executionDurationMs": 362,
        "success": true,
        "errorMessage": null
      }
    ],
    "liveModelUsed": true,
    "modelName": "llama3.2:3b",
    "grounded": true,
    "timestamp": "2026-10-14T20:12:11"
  },
  "timestamp": "2026-10-14T20:12:11"
}
```

### Natural Language Question (Find Eligible Candidates)
**Request**:
```http
POST /api/assistant/chat HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "message": "Find eligible candidates for demand 50"
}
```

---

## 7. Database Safety & Test Isolation

To safeguard live demo records, automated tests are programmatically isolated from `talent_marketplace`:

1. **Dedicated Test Database**: `talent_marketplace_test` (`src/test/resources/application.properties`).
2. **Programmatic Safety Guard**: `TestDatabaseSafetyGuard` inspects the active JDBC connection catalog on context initialization and before every `@BeforeEach cleanDatabase()`. If connected to `talent_marketplace`, tests abort immediately with an `IllegalStateException`.
3. **Running the Full Test Suite Safely**:
   ```powershell
   .\mvnw.cmd clean test
   ```
   All 138 tests run exclusively against `talent_marketplace_test`.

---

## 8. Limitations & Error Handling

- **Local Model Performance**: Inference runs on the local CPU or GPU. Generation time typically takes 2–8 seconds depending on hardware.
- **Read-Only Assistant**: The assistant cannot directly create, approve, or cancel allocations; all state changes must be confirmed via the authorized `/api/allocations` endpoints.
- **Offline / Unavailable Ollama**: If Ollama is not running, `POST /api/assistant/chat` returns HTTP 503 (`Service Unavailable`) with clear diagnostic instructions (`ollama serve`). The assistant will never falsely claim a live model was used.
