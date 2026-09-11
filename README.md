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
Service Layer (Business Logic, SKU Uniqueness & Transactions)
   ↓ Domain Entities
Repository Layer (Spring Data JPA / Hibernate)
   ↓ SQL Queries via JDBC Driver
PostgreSQL Database (factoryos)
```

### Layer Responsibilities

1. **Client**: Interacts with the backend over HTTP using JSON payloads.
2. **REST Controller (`com.factoryos.controller`)**:
   - Handles HTTP routing, input parsing, serialization/deserialization.
   - Enforces Jakarta Bean Validation (`@Valid`) on incoming requests.
   - Converts service outputs into HTTP responses (`ResponseEntity`).
3. **Service Layer (`com.factoryos.service`)**:
   - Encapsulates all domain and business rules (e.g. SKU normalization and uniqueness checking, soft deletion).
   - Manages transactional boundaries (`@Transactional` and `@Transactional(readOnly = true)`).
4. **Repository Layer (`com.factoryos.repository`)**:
   - Provides data access abstractions using Spring Data JPA interfaces.
   - Executes database operations via Hibernate ORM.
5. **PostgreSQL Database**:
   - Relational database responsible for durable persistence and table-level constraints (e.g. unique constraint on `sku`).
6. **Data Transfer Objects (`com.factoryos.dto`)**:
   - Decouples external API representations from internal JPA entities, preventing accidental exposure of internal entity state and guarding against mass-assignment vulnerabilities.
7. **Exception Handling (`com.factoryos.exception`)**:
   - Centralized `@RestControllerAdvice` mapping domain exceptions to uniform error envelopes (`ErrorResponse`).

---

## Features

### Currently Implemented (Phase 1 & Phase 2)

- [x] **Project Foundation**: Java 21 LTS, Spring Boot 3.4.x, Maven Wrapper (`mvnw`), PostgreSQL JDBC.
- [x] **Product Management**:
  - Full CRUD operations with RESTful conventions.
  - SKU normalization and uniqueness enforced at both application service and database constraint levels.
  - Immutable SKU policy on product updates.
  - Soft deletion / deactivation (`active = false`, HTTP 204 No Content).
  - Search products by name substring (`GET /api/products/search?name=...`).
  - Active-only product filtering (`GET /api/products/active`).
  - Monetary precision using `BigDecimal` (`precision = 12, scale = 2`).
- [x] **Supplier Management**:
  - Full CRUD operations with RESTful conventions.
  - Email format validation.
  - Soft deletion / deactivation (`active = false`, HTTP 204 No Content).
  - Active-only supplier filtering (`GET /api/suppliers/active`).
- [x] **Centralized Exception Handling**: Standardized error responses (`ErrorResponse`) for 400 (Validation), 404 (Not Found), 409 (Conflict), and 500 (Internal Error).
- [x] **Automated Testing Suite**: 30 tests covering unit services (Mockito), WebMvc slice tests (MockMvc), and full context integration testing against PostgreSQL.
- [x] **Health Check Endpoint**: `GET /api/health` returning operational status.

### Planned Features (Upcoming Phases)

- [ ] **Inventory Tracking**: Real-time stock levels, available vs. reserved quantities
- [ ] **Stock Movements**: Auditable transaction logs (`STOCK_IN`, `STOCK_OUT`, `ADJUSTMENT`)
- [ ] **Purchase Orders**: Procurement workflows from draft to receiving and inventory updates
- [ ] **Low-Stock Detection**: Automated detection of inventory falling below defined reorder thresholds
- [ ] **Authentication & Authorization**: Role-based access control and security

---

## Tech Stack

| Technology                  | Purpose                                                                            |
| :-------------------------- | :--------------------------------------------------------------------------------- |
| **Java 21**                 | Modern LTS Java runtime (records, pattern matching)                                |
| **Spring Boot 3.4.x**       | Enterprise application framework                                                   |
| **Spring Web**              | RESTful web services and MVC architecture                                          |
| **Spring Data JPA**         | Repository abstraction and database access                                         |
| **Hibernate 6.x**           | Object-Relational Mapping (ORM) and schema management                              |
| **PostgreSQL 16**           | Relational SQL database engine                                                     |
| **Jakarta Bean Validation** | Declarative data validation annotations (`@NotBlank`, `@PositiveOrZero`, `@Email`) |
| **Lombok**                  | Boilerplate reduction for entities                                                 |
| **Maven**                   | Dependency management and build automation                                         |
| **JUnit 5 & Mockito**       | Automated unit and integration testing                                             |

---

## Project Structure

```text
FactoryOS
├── .docs/
│   ├── master.md                       # Master project specification
│   └── walkthroughs/                   # Phase walkthroughs and installation guides
│       ├── phase-1-walkthrough.md
│       ├── phase-2-walkthrough.md
│       └── installation-and-guide.md
├── .mvn/wrapper/                       # Maven wrapper binaries and configuration
├── src/
│   ├── main/
│   │   ├── java/com/factoryos/
│   │   │   ├── controller/             # REST Controllers
│   │   │   │   ├── HealthController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   └── SupplierController.java
│   │   │   ├── dto/                    # Data Transfer Objects (Java Records)
│   │   │   │   ├── CreateProductRequest.java
│   │   │   │   ├── UpdateProductRequest.java
│   │   │   │   ├── ProductResponse.java
│   │   │   │   ├── CreateSupplierRequest.java
│   │   │   │   ├── UpdateSupplierRequest.java
│   │   │   │   ├── SupplierResponse.java
│   │   │   │   └── HealthResponse.java
│   │   │   ├── entity/                 # JPA Entities
│   │   │   │   ├── Product.java
│   │   │   │   └── Supplier.java
│   │   │   ├── exception/              # Exceptions & Global Exception Handler
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── BusinessRuleException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── mapper/                 # Entity-DTO Mappers
│   │   │   │   ├── ProductMapper.java
│   │   │   │   └── SupplierMapper.java
│   │   │   ├── repository/             # Spring Data JPA Repositories
│   │   │   │   ├── ProductRepository.java
│   │   │   │   └── SupplierRepository.java
│   │   │   ├── service/                # Business Logic Services
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── ProductServiceImpl.java
│   │   │   │   ├── SupplierService.java
│   │   │   │   └── SupplierServiceImpl.java
│   │   │   └── FactoryOsApplication.java # Main Application Class
│   │   └── resources/
│   │       └── application.properties  # Application & Database properties
│   └── test/
│       └── java/com/factoryos/
│           ├── controller/             # WebMvc MockMvc Tests
│           │   ├── HealthControllerTest.java
│           │   ├── ProductControllerTest.java
│           │   └── SupplierControllerTest.java
│           ├── service/                # Service Layer Mockito Unit Tests
│           │   ├── ProductServiceTest.java
│           │   └── SupplierServiceTest.java
│           └── FactoryOsApplicationTests.java # Context Load & Integration Test
├── .env.example                        # Template for environment variables
├── .gitignore                          # Git exclusions
├── mvnw / mvnw.cmd                     # Maven wrapper scripts
├── pom.xml                             # Maven project configuration
├── README.md                           # Repository documentation
└── prompt1.md                          # Phase 1 specification
```

---

## REST API Reference & Examples

### Products

#### 1. Create Product

- **Method**: `POST`
- **Path**: `/api/products`
- **Request Body**:

```json
{
  "sku": "BRG-6204",
  "name": "Steel Bearing 6204",
  "description": "Industrial deep-groove bearing",
  "category": "Bearings",
  "unitPrice": 450.0,
  "reorderLevel": 20
}
```

- **Response (`201 Created`)**:

```json
{
  "id": 1,
  "sku": "BRG-6204",
  "name": "Steel Bearing 6204",
  "description": "Industrial deep-groove bearing",
  "category": "Bearings",
  "unitPrice": 450.0,
  "reorderLevel": 20,
  "active": true,
  "createdAt": "2026-09-11T08:28:05.501742Z",
  "updatedAt": "2026-09-11T08:28:05.501742Z"
}
```

#### 2. Duplicate SKU Conflict

- **Method**: `POST`
- **Path**: `/api/products` with existing SKU
- **Response (`409 Conflict`)**:

```json
{
  "timestamp": "2026-09-11T08:28:05.615788Z",
  "status": 409,
  "error": "Conflict",
  "message": "Product with SKU 'BRG-6204' already exists",
  "path": "/api/products"
}
```

#### 3. Update Product (SKU is Immutable)

- **Method**: `PUT`
- **Path**: `/api/products/{id}`
- **Request Body**:

```json
{
  "name": "Steel Bearing 6204 Heavy Duty",
  "description": "Upgraded heavy duty bearing",
  "category": "Bearings",
  "unitPrice": 495.0,
  "reorderLevel": 25,
  "active": true
}
```

- **Response (`200 OK`)**: Updated product record with SKU preserved.

#### 4. Deactivate Product (Soft Delete)

- **Method**: `DELETE`
- **Path**: `/api/products/{id}`
- **Response (`204 No Content`)**: Empty body. Record remains in database with `active = false`.

#### 5. Search Products by Name

- **Method**: `GET`
- **Path**: `/api/products/search?name=bearing`
- **Response (`200 OK`)**: List of matching products.

---

### Suppliers

#### 1. Create Supplier

- **Method**: `POST`
- **Path**: `/api/suppliers`
- **Request Body**:

```json
{
  "name": "ABC Industrial Supplies",
  "contactPerson": "Rahul Sen",
  "email": "contact@abcindustrial.example",
  "phone": "+91-9876543210",
  "address": "Industrial Area, Kolkata"
}
```

- **Response (`201 Created`)**:

```json
{
  "id": 1,
  "name": "ABC Industrial Supplies",
  "contactPerson": "Rahul Sen",
  "email": "contact@abcindustrial.example",
  "phone": "+91-9876543210",
  "address": "Industrial Area, Kolkata",
  "active": true,
  "createdAt": "2026-09-11T08:28:05.782812Z",
  "updatedAt": "2026-09-11T08:28:05.782812Z"
}
```

#### 2. Deactivate Supplier (Soft Delete)

- **Method**: `DELETE`
- **Path**: `/api/suppliers/{id}`
- **Response (`204 No Content`)**: Soft deactivates the supplier (`active = false`).

#### 3. Get Active Suppliers

- **Method**: `GET`
- **Path**: `/api/suppliers/active`
- **Response (`200 OK`)**: List containing only active suppliers.

---

## Setup & Running

### 1. Configure Environment Variables

Copy `.env.example` to `.env` and set your PostgreSQL credentials:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/factoryos"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_password"
```

### 2. Run Tests

```bash
./mvnw clean test
```

### 3. Start Application

```bash
./mvnw spring-boot:run
```

The server starts on port `8080`.
