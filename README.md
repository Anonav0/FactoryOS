# FactoryOS — Industrial Inventory Management System

FactoryOS is a robust, production-ready backend system designed for managing industrial inventory, tracking stock movements, overseeing supplier relations, and streamlining procurement workflows in a manufacturing or factory setting.

This project is built using modern Java 21 and Spring Boot 3.x, adhering strictly to clean code principles, layered architecture, and enterprise best practices.

---

## Architecture Overview

FactoryOS follows a classic, clean **Layered Architecture** ensuring separation of concerns, high testability, and clear boundaries across system components:

```text
Client (Web / Mobile / Third-Party API)
   ↓ HTTP / JSON Requests
REST Controller Layer
   ↓ DTOs / Method Invocations
Service Layer (Business Logic & Transactions)
   ↓ Domain Entities
Repository Layer (Spring Data JPA / Hibernate)
   ↓ SQL Queries via JDBC Driver
PostgreSQL Database
```

### Layer Responsibilities

1. **Client**: Interacts with the backend over HTTP using JSON payloads.
2. **REST Controller (`com.factoryos.controller`)**:
   - Handles HTTP routing, input parsing, serialization/deserialization.
   - Enforces Jakarta Bean Validation on incoming requests.
   - Converts service outputs into HTTP responses (`ResponseEntity`).
3. **Service Layer (`com.factoryos.service`)**:
   - Encapsulates all domain and business rules.
   - Coordinates multi-entity workflows and manages transactional boundaries (`@Transactional`).
   - Ensures validation rules that depend on persistent state (e.g., verifying sufficient stock before deduction).
4. **Repository Layer (`com.factoryos.repository`)**:
   - Provides data access abstractions using Spring Data JPA.
   - Executes database operations via Hibernate ORM.
5. **PostgreSQL Database**:
   - The relational database system responsible for durable data persistence and referential integrity.
6. **Data Transfer Objects (`com.factoryos.dto`)**:
   - Decouples external API representations from internal JPA entities, preventing accidental exposure of sensitive entity state or internal database schemas.

---

## Features

### Currently Implemented (Phase 1: Project Setup & Architecture)

- [x] Modern Java 21 and Spring Boot 3.x foundation
- [x] Maven build configuration with self-contained Maven Wrapper (`mvnw`)
- [x] Clean layered architecture structure
- [x] Environment-variable driven PostgreSQL database configuration (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- [x] JPA / Hibernate configuration with schema update strategy
- [x] Health check monitoring endpoint (`GET /api/health`)
- [x] Automated testing suite (Application Context loading test and WebMvc endpoint test)
- [x] Developer tooling: `.env.example`, `.gitignore`, and comprehensive documentation

### Planned Features (Upcoming Phases)

- [ ] **Product Management**: Full CRUD, SKU uniqueness, categorization, pricing, and reorder levels
- [ ] **Supplier Management**: Supplier profiles, contact info validation, and active status tracking
- [ ] **Inventory Tracking**: Real-time stock levels, available vs. reserved quantities
- [ ] **Stock Movements**: Auditable transaction logs (`STOCK_IN`, `STOCK_OUT`, `ADJUSTMENT`)
- [ ] **Purchase Orders**: Procurement workflows from draft to receiving and inventory updates
- [ ] **Low-Stock Alerts**: Automatic detection of inventory falling below defined reorder thresholds
- [ ] **Global Exception Handling**: Centralized error envelopes and standardized REST error responses
- [ ] **Comprehensive Test Coverage**: Unit tests with Mockito and integration tests for core business flows

---

## Tech Stack

| Technology                  | Purpose                                                                         |
| :-------------------------- | :------------------------------------------------------------------------------ |
| **Java 21**                 | Modern LTS Java runtime (records, pattern matching, virtual threads capability) |
| **Spring Boot 3.4.x**       | Enterprise application framework                                                |
| **Spring Web**              | RESTful web services and MVC architecture                                       |
| **Spring Data JPA**         | Repository abstraction and database access                                      |
| **Hibernate 6.x**           | Object-Relational Mapping (ORM) and schema management                           |
| **PostgreSQL**              | Relational SQL database engine                                                  |
| **Jakarta Bean Validation** | Declarative data validation annotations                                         |
| **Lombok**                  | Boilerplate reduction for accessors and constructors                            |
| **Maven**                   | Dependency management and build automation                                      |
| **JUnit 5 & Mockito**       | Automated unit and integration testing                                          |

---

## Project Structure

```text
FactoryOS
├── .docs/
│   ├── master.md                       # Project master specification
│   └── walkthroughs/                   # Phase walkthroughs and installation guides
│       ├── phase-1-walkthrough.md
│       └── installation-and-guide.md
├── .mvn/wrapper/                       # Maven wrapper binaries and configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/factoryos/
│   │   │       ├── controller/         # REST Controllers (HealthController)
│   │   │       ├── service/            # Business logic services (Phase 2+)
│   │   │       ├── repository/         # Spring Data JPA Repositories (Phase 2+)
│   │   │       ├── entity/             # JPA Entities (Phase 2+)
│   │   │       ├── dto/                # Data Transfer Objects (HealthResponse)
│   │   │       ├── mapper/             # Entity-DTO Mappers (Phase 2+)
│   │   │       ├── exception/          # Global Exception Handlers (Phase 2+)
│   │   │       ├── validation/         # Custom Validators (Phase 2+)
│   │   │       ├── config/             # Application Configurations (Phase 2+)
│   │   │       └── FactoryOsApplication.java # Spring Boot Main Entry Point
│   │   └── resources/
│   │       └── application.properties  # Database & JPA properties
│   └── test/
│       └── java/
│           └── com/factoryos/
│               ├── FactoryOsApplicationTests.java  # Spring Context Load Test
│               └── controller/
│                   └── HealthControllerTest.java   # Health endpoint WebMvc test
├── .env.example                        # Template for environment configuration
├── .gitignore                          # Git exclusions
├── mvnw / mvnw.cmd                     # Maven wrapper scripts
├── pom.xml                             # Maven project descriptor
├── prompt1.md                          # Phase 1 specification
└── README.md                           # Repository documentation
```

---

## Setup & Prerequisites

### 1. Install Java 21

Ensure OpenJDK 21 is installed:

```bash
java -version
```

### 2. Install & Start PostgreSQL

Install PostgreSQL (version 14+) on your system:

```bash
# Ubuntu / Debian
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

### 3. Create the Database

Connect to PostgreSQL and create the `factoryos` database:

```sql
CREATE DATABASE factoryos;
```

Command-line alternative:

```bash
createdb -U postgres factoryos
```

### 4. Configure Environment Variables

Copy `.env.example` to `.env` or export the variables in your shell:

```bash
cp .env.example .env
```

Set the values corresponding to your local PostgreSQL instance:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/factoryos"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_password"
```

> **Note**: `application.properties` supplies safe local defaults (`localhost:5432/factoryos`, user `postgres`, password `postgres`), allowing direct startup if standard local credentials are used.

---

## Running the Application

Build the project and run all tests:

```bash
./mvnw clean test
```

Start the Spring Boot application:

```bash
./mvnw spring-boot:run
```

The application will start on port `8080`.

---

## Health Check Verification

Once running, verify that the application and its REST layer are operational:

### Request

```bash
curl -i http://localhost:8080/api/health
```

### Response

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "UP",
  "application": "FactoryOS"
}
```
