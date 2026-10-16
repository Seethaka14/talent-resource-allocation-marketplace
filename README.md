# AI-Driven Talent and Resource Allocation Marketplace

A web application that helps project managers identify suitable employees for project demands based on skills, availability, and location. It includes an AI staffing assistant to support candidate comparisons and staffing-related questions.

## 1. Project Overview

The marketplace brings employee information, skills, project demands, candidate recommendations, availability, and resource allocations into one application. It uses a Spring Boot backend, a web frontend, MySQL for data storage, and a local Ollama model for AI-assisted staffing.

## 2. Prerequisites

- **Java JDK 25** — the version configured for this project.
- **MySQL 8.x** — application database.
- **Ollama** with the `llama3.2:3b` model — required for AI features.
- **Web browser** — to access the application.
- **Code editor or terminal** — for configuration and commands.

The project includes the Maven Wrapper (`mvnw` and `mvnw.cmd`), so a separate Maven installation is generally not required. Spring Boot, Spring Data JPA, Hibernate, and test libraries are managed through `pom.xml`.

## 3. Key Features

- Manage employees, skills, and project demands.
- Recommend and rank eligible employees based on skills, availability, and location.
- Check employee availability and manage resource allocations.
- View resource utilization and skill-gap analytics.
- Use the AI staffing assistant to compare candidates and explain missing skills.
- Apply role-based access for Project Managers and Resource Managers.
- Protect allocation capacity during concurrent allocation confirmations.

## 4. System Architecture

The application follows a **layered architecture** that separates request handling, business logic, and database access.

```text
          Project Manager / Resource Manager
                         |
                         v
                  Web Interface
                 (HTML / CSS / JS)
                         |
                         v
                Spring Security
          (Authentication and Authorization)
                         |
                         v
                  Controllers
                  (REST API Layer)
                         |
                         v
                    Services
          (Business Logic and Validation)
                         |
                         v
                  Repositories
                 (Spring Data JPA)
                         |
                         v
                   MySQL Database


              AI Staffing Assistant
                         |
                         v
                 Assistant Service
                         |
                         v
              Allowlisted Read-Only Tools
                         |
                         v
                Local Ollama Model
                  (llama3.2:3b)
```

- **Web interface:** Provides screens for employees, skills, demands, recommendations, allocations, and analytics.
- **Security:** Authenticates users and enforces role-based access.
- **Controllers:** Receive HTTP requests and return API responses.
- **Services:** Implement business rules, ranking, availability checks, and allocation validation.
- **Repositories:** Access database records through Spring Data JPA and Hibernate.
- **MySQL:** Stores application data.
- **AI assistant:** Uses the local Ollama model and allowlisted read-only tools to answer staffing questions.

**Request flow:** A request from the web interface passes through security to a controller. The service layer applies business rules, and the repository accesses MySQL when needed. The result is returned to the frontend. During allocation confirmation, the service locks and revalidates the relevant employee capacity to help prevent conflicting concurrent allocations.

## 5. Tech Stack

| Area | Technology |
|---|---|
| Programming language | Java 25 |
| Backend framework | Spring Boot 4.1.1 |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL 8.x |
| Database migrations | Flyway |
| Frontend | HTML, CSS, JavaScript |
| AI integration | Ollama, `llama3.2:3b` |
| Build and testing | Maven Wrapper, JUnit and project test dependencies |
| API testing | Postman |

## 6. Setup

### Step 1: Install prerequisites
Install Java JDK 25, MySQL 8.x, and Ollama if you plan to use the AI assistant.

### Step 2: Create the database
If the database does not already exist, create it in MySQL:

```sql
CREATE DATABASE talent_marketplace;
```

### Step 3: Configure database access
Configure the datasource URL, username, and password in `src/main/resources/application.properties` or through the environment variables supported by the project. Do not commit real credentials.

### Step 4: Check database migrations
Flyway is disabled by default in the main configuration to protect existing demo data. For a new database, review the migration scripts and project configuration before applying migrations. Do not enable migrations against an existing database without checking its schema and migration history first.

### Step 5: Set up the AI model
Pull the model:

```bash
ollama pull llama3.2:3b
```

Ensure Ollama is running at the endpoint configured for the application (commonly `http://localhost:11434`). The core application can still be used when the local AI service is unavailable, but AI requests may not work.

## 7. Run Instructions

Open a terminal in the project root and start the application:

**Windows PowerShell**
```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux**
```bash
./mvnw spring-boot:run
```

The application is configured to run at:

```text
http://localhost:8080
```

Stop the application with `Ctrl+C`.


## 8. Login and Role-Based Access

The application supports two roles:

| Role | Main access |
|---|---|
| **Project Manager (PM)** | Manage project demands and view candidate recommendations. |
| **Resource Manager (RM)** | Manage resource allocations and approval workflows. |

### Login URLs

Start the application first, then open the relevant login page:

- **Project Manager:** `http://localhost:8080/login-pm.html`
- **Resource Manager:** `http://localhost:8080/login-rm.html`

These URLs correspond to the role-specific login page filenames in `src/main/resources/static/`. Verify each page loads successfully in your local environment.

### Demo credentials

Use the local demo accounts configured by `DemoAccountInitializer.java`. Confirm the current login identifiers and passwords from the local configuration before sharing them. Do not publish database credentials, real user passwords, or other secrets in this README. Demo credentials are intended for local evaluation only and must not be used in a deployed environment.


## 9. Project Folder Structure

```text
talent-resource-marketplace/
├── .mvn/                             # Maven Wrapper configuration
├── docs/
│   ├── postman/                      # Postman API collection
│   ├── screenshots/                  # Application screenshots and test evidence
│   ├── SCREENSHOTS.md                # Screenshot documentation
│   └── SUBMISSION_CHECKLIST.md       # Submission checklist
├── src/
│   ├── main/
│   │   ├── java/com/talentmarketplace/
│   │   │   ├── ai/                   # AI assistant components
│   │   │   ├── config/               # Application configuration
│   │   │   ├── controller/           # REST API endpoints
│   │   │   ├── dto/                  # Request and response objects
│   │   │   ├── entity/               # JPA entities
│   │   │   ├── exception/            # Exception handling
│   │   │   ├── repository/           # Database access
│   │   │   ├── service/              # Business logic
│   │   │   └── TalentResourceMarketplaceApplication.java
│   │   └── resources/
│   │       ├── db/                   # Database scripts and migrations
│   │       ├── static/               # Static frontend resources
│   │       ├── templates/            # HTML templates
│   │       └── application.properties
│   └── test/
│       ├── java/                     # Automated tests
│       └── resources/                # Test resources
├── .gitattributes                    # Git file-handling rules
├── .gitignore                        # Files excluded from Git
├── AI_USAGE_LOG.md                   # AI usage documentation
├── DEMO_PLAN.md                      # Demo walkthrough
├── HELP.md                           # Project help information
├── mvnw                              # Maven Wrapper for Unix
├── mvnw.cmd                          # Maven Wrapper for Windows
├── pom.xml                           # Dependencies and build configuration
└── README.md                         # Project documentation
```

## 10. API Details

**Base URL:** `http://localhost:8080`

The REST API includes endpoints for:

- Authentication and current-user profile
- Employees and employee skills
- Skills and project demands
- Candidate recommendations
- Employee unavailability
- Resource allocations
- Analytics and reports
- AI staffing assistant

For exact endpoint paths, HTTP methods, request bodies, and example responses, import this collection into Postman:

`docs/postman/talent_resource_marketplace.postman_collection.json`

Protected endpoints require an authenticated session and the appropriate role. Use the Postman collection as the endpoint reference; do not assume every request has been verified unless it has been run.

## 11. Test Commands

Run the automated test suite from the project root:

```powershell
.\mvnw.cmd clean test
```

On macOS or Linux:

```bash
./mvnw clean test
```

The latest reported test run completed with **223 tests, 0 failures, 0 errors, and 0 skipped**, ending in `BUILD SUCCESS`. Run the command again in your environment before submission to confirm the current result.

## Additional Documentation

- [AI Usage Log](AI_USAGE_LOG.md)
- [Demo Plan](DEMO_PLAN.md)
- [Screenshot Documentation](docs/SCREENSHOTS.md)
- [Submission Checklist](docs/SUBMISSION_CHECKLIST.md)
- Postman collection: `docs/postman/talent_resource_marketplace.postman_collection.json`
