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
   ├── ProductController (/api/products)
   ├── SupplierController (/api/suppliers)
   ├── InventoryController (/api/inventory)
   └── HealthController (/api/health)
   ↓ DTOs / Method Invocations
Service Layer (Business Logic, SKU Uniqueness, Transactions, Concurrency)
   ├── ProductService
   ├── SupplierService
   └── InventoryService
   ↓ Domain Entities
Repository Layer (Spring Data JPA / Hibernate)
   ├── ProductRepository
   ├── SupplierRepository
   ├── InventoryRepository
   └── StockMovementRepository
   ↓ SQL Queries via JDBC Driver
PostgreSQL Database (factoryos)
   ├── products
   ├── suppliers
   ├── inventory
   └── stock_movements
```

### Layer Responsibilities

1. **Client**: Interacts with the backend over HTTP using JSON payloads.
2. **REST Controller (`com.factoryos.controller`)**:
   - Handles HTTP routing, input parsing, serialization/deserialization.
   - Enforces Jakarta Bean Validation (`@Valid`) on incoming requests.
   - Converts service outputs into HTTP responses (`ResponseEntity`).
3. **Service Layer (`com.factoryos.service`)**:
   - Encapsulates domain logic (SKU uniqueness, negative-stock prevention, stock adjustments, low-stock evaluation).
   - Coordinates multi-entity workflows and manages transactional boundaries (`@Transactional` and `@Transactional(readOnly = true)`).
4. **Repository Layer (`com.factoryos.repository`)**:
   - Provides data access abstractions using Spring Data JPA.
   - Performs database-side optimizations (e.g. low-stock JPQL filtering).
5. **PostgreSQL Database**:
   - Durable relational persistence, foreign key cascades, unique constraints, and check constraints.
6. **Data Transfer Objects (`com.factoryos.dto`)**:
   - Decouples external API representations from internal JPA entities, guarding against mass-assignment and circular reference issues.
7. **Exception Handling (`com.factoryos.exception`)**:
   - Centralized `@RestControllerAdvice` mapping domain exceptions (`InsufficientStockException`, `ResourceNotFoundException`, `DuplicateResourceException`) to uniform error envelopes (`ErrorResponse`).

---

## Features

### Currently Implemented (Phases 1, 2, 3, 3.5, 4, 5, 6, 7, 8 & 9)

- [x] **Production Hardening (Phase 9)**:
  - Configuration externalization with environment variable overrides (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SERVER_PORT`, `CORS_ALLOWED_ORIGINS`, `JPA_DDL_AUTO`, `SWAGGER_ENABLED`).
  - Production profile separation (`application-prod.properties`) enforcing `spring.jpa.hibernate.ddl-auto=validate` so production never modifies schemas at startup.
  - Database check constraints (`@Check`) on prices, quantities, and totals across `Product`, `Inventory`, `PurchaseOrder`, and `PurchaseOrderItem`.
  - Configurable CORS origin protection via validated `FactoryOsProperties` without wildcard exposure.
  - SLF4J audit logging across all domain services without leaking credentials or internal details.
  - Purchase order line-item capacity safety limits (`@Size(min = 1, max = 100)`).
  - Optimistic locking collision handling translating to HTTP `409 Conflict`.
  - Automated test suite expanded to **116 tests** (100% passing) and **86% JaCoCo instruction coverage**.
- [x] **API Documentation & Demo Data (Phase 8)**:
  - Full OpenAPI 3.1 & Swagger UI integration via `springdoc-openapi-starter-webmvc-ui` (v2.8.5) accessible at `/swagger-ui/index.html` and `/v3/api-docs`.
  - Comprehensive API documentation across 5 core domain tags: `Products`, `Suppliers`, `Inventory`, `Purchase Orders`, and `System`.
  - Rich business behavior explanations: SKU uniqueness, soft deletion, negative stock guards, sequential order numbering, authoritative `BigDecimal` totals, and state machine transitions.
  - Complete schema models with descriptive field explanations and realistic industrial examples for all request and response DTOs, including standardized `ErrorResponse` models.
  - Environment-aware demo data seeder (`DevelopmentDataInitializer`) active under `dev`/`demo` profiles:
    - 8 realistic industrial products with varied categories, pricing, and reorder levels.
    - 5 realistic suppliers with complete contacts and addresses.
    - Multi-tier inventory dataset featuring healthy stock, low-stock alerts, and a zero-stock item.
    - Audit movement trail with realistic `STOCK_IN`, `STOCK_OUT`, and `ADJUSTMENT` entries.
    - 4 purchase orders representing all 4 workflow states (`CREATED`, `APPROVED`, `RECEIVED`, `CANCELLED`).
  - Production safety & idempotency: checks for existing data prior to seeding to prevent duplicate records across multiple restarts; never runs in production.
  - Test suite expanded to **109 tests** (100% passing) and **85% overall instruction coverage**.
- [x] **Testing & Business Logic Verification (Phase 7)**:
  - Robust automated testing pyramid with **103 tests** across Service Unit Tests (Mockito), Controller Slice Tests (`@WebMvcTest`), and Spring Boot Integration Tests (`@SpringBootTest` with live PostgreSQL).
  - Business behavior focus: stock depletion boundaries, zero/positive inventory adjustments, inactive product guards, low-stock reorder thresholds, sequential PO numbering, multi-item line totals, and full state machine transitions.
  - End-to-end atomic `@Transactional` rollback proof: verifies against real PostgreSQL that if any item in a multi-item goods receipt fails, all previous item stock increments, movements, and PO status mutations are completely rolled back.
  - Reusable test fixtures in `TestDataFactory` eliminating boilerplate entity instantiation across test suites.
  - Configured JaCoCo test coverage plugin (`jacoco-maven-plugin` 0.8.12) achieving **83% overall instruction coverage** (Controllers 89%, Services 87%, Exceptions 84%, DTOs 100%).
- [x] **Validation & Exception Handling Hardening (Phase 6)**:
  - Standardized API error envelopes (`ErrorResponse`) featuring UTC timestamp, HTTP status code, error phrase, descriptive message, request path, and structured field-level validation errors (`errors` & `validationErrors`).
  - Strict input validation using Jakarta Validation annotations on all request DTOs, including nested collection validation on purchase order items (`@Valid @NotEmpty`).
  - Path variable and query parameter validation via `@Validated` on controllers (`@Positive` IDs, `@NotBlank` order numbers).
  - Clean base exception hierarchy (`ApplicationException extends RuntimeException`) grouping all custom domain exceptions.
  - Comprehensive `@RestControllerAdvice` global exception handling:
    - `400 Bad Request`: Field validation failures, parameter constraint violations, malformed JSON bodies (`HttpMessageNotReadableException`), type conversion mismatches (`MethodArgumentTypeMismatchException`), missing parameters (`MissingServletRequestParameterException`), and business rule violations.
    - `404 Not Found`: Missing domain resources (`ResourceNotFoundException`, `InventoryNotFoundException`) and unmatched API endpoints (`NoResourceFoundException`, `NoHandlerFoundException`).
    - `405 Method Not Allowed`: Unsupported HTTP methods (`HttpRequestMethodNotSupportedException`).
    - `409 Conflict`: Duplicate resources, insufficient stock, invalid PO state transitions, optimistic locking collisions (`ObjectOptimisticLockingFailureException`), and database constraint violations (`DataIntegrityViolationException`).
    - `500 Internal Server Error`: Catch-all fallback that logs full stack traces server-side while returning generic, sanitized error messages to clients without leaking SQL, table names, or internal class names.
  - Frontend integration: Centralized error handling in `client.js` extracting field validation errors directly to form inputs and routing general business errors to toast alerts.
  - Automated test suite expanded to **92 tests** (100% passing), including a dedicated `GlobalExceptionHandlerTest` testing every error translation branch.
- [x] **Purchase Order Workflow (Phase 5)**:
  - Formal domain state machine: `CREATED -> APPROVED -> RECEIVED`, `CREATED -> CANCELLED`, `APPROVED -> CANCELLED`.
  - Strict terminal state guarantees: orders in `RECEIVED` or `CANCELLED` cannot transition further; invalid transitions are rejected with HTTP 409 Conflict.
  - Non-generic status updates: state transitions are isolated behind dedicated business endpoints (`approve`, `receive`, `cancel`), preventing arbitrary status modifications.
  - Fully atomic `@Transactional` receiving boundary: increments `Inventory.quantityAvailable`, generates individual `STOCK_IN` audit records per PO item, and transitions PO status to `RECEIVED` as the final step.
  - Rollback safety: if any item or inventory update fails, all database changes roll back completely, preventing partial stock updates.
  - Cancellation safety: cancelling an order updates status to `CANCELLED` without mutating inventory or recording stock movements; cancellation of received orders is strictly disallowed.
  - Optimistic locking via `@Version` on `PurchaseOrder` preventing concurrent receiving collisions.
  - Frontend workflow integration: dynamic action buttons (`Approve`, `Receive`, `Cancel`, `Details`), destructive/permanent action confirmation modals, accessible badges, and auto-dismissing toast notifications.
- [x] **Purchase Order Management (Phase 4)**:
  - Multi-item Purchase Orders linked to Suppliers (`ManyToOne`) and Products (`ManyToOne`).
  - Strict inventory isolation: PO creation records procurement intent without mutating inventory balances.
  - Automatic sequential order number generation (`PO-000001`, `PO-000002`).
  - Historical unit price snapshotting on each `PurchaseOrderItem`.
  - Authoritative backend financial calculations using `BigDecimal` (`subtotal = quantity × unitPrice`, `totalAmount = sum(subtotals)`).
  - Business validations: enforces active suppliers, active products, positive quantities, and rejects duplicate products within a single PO.
  - Initial `CREATED` status lifecycle modeling.
  - Frontend integration: Dynamic multi-item PO creation modal, line item subtotal calculations, live total guidance, and order details modal.
- [x] **Frontend Operations Dashboard (Phase 3.5)**:
  - Desktop-first, clean industrial operations UI built with React & Vite.
  - 5 Core Views: Dashboard (KPI cards + Low Stock table), Products, Suppliers, Inventory, Purchase Orders.
  - Centralized API layer (`src/api`) consuming Spring Boot REST APIs with CORS support.
  - Form validation with inline field errors mapped from backend `ErrorResponse`.
  - Negative-stock prevention with clear 409 conflict alerts.
  - Interactive modals for Stock-In, Stock-Out, Adjustments, and chronological Movement History.
  - Soft deactivation flows with explicit confirmation modals.
  - Auto-dismissing toast notifications and accessible loading/empty states.
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
- [x] **Inventory Management**:
  - 1:1 relationship between Product and Inventory.
  - Automatic inventory record initialization on product creation (`quantity = 0, reserved = 0`).
  - Stock-in operations (`POST /api/inventory/stock-in`).
  - Stock-out operations with negative-stock prevention (`POST /api/inventory/stock-out`).
  - Stock adjustments to physical count (`POST /api/inventory/adjust`).
  - Complete, immutable audit log via `StockMovement` records (`STOCK_IN`, `STOCK_OUT`, `ADJUSTMENT`).
  - Real-time low-stock detection (`quantityAvailable <= reorderLevel`) queried at the database layer (`GET /api/inventory/low-stock`).
  - Optimistic locking via `@Version` to protect against concurrent update collisions.
  - Restriction of stock movements on inactive products.
- [x] **Centralized Exception Handling**: Uniform error envelopes (`ErrorResponse`) for 400 (Validation / Rule / Malformed), 404 (Not Found), 405 (Method Not Allowed), 409 (Conflict / Insufficient Stock / Duplicate / Invalid PO State), and 500 (Sanitized Server Error).
- [x] **Automated Testing Suite**: 92 tests covering unit services (Mockito), WebMvc slice tests (MockMvc), exception handlers, and full context integration testing against PostgreSQL.
- [x] **Health Check Endpoint**: `GET /api/health` returning operational status.

### Planned Features (Upcoming Phases)

- [ ] **Supplier Catalog Integration (Phase 7)**: Product-supplier pricing agreements and lead times
- [ ] **Authentication & Authorization**: Role-based access control and security

---

## Tech Stack

| Technology                  | Purpose                                                                     |
| :-------------------------- | :-------------------------------------------------------------------------- |
| **Java 21**                 | Modern LTS Java runtime (records, pattern matching)                         |
| **Spring Boot 3.4.x**       | Enterprise application framework                                            |
| **Spring Web**              | RESTful web services and MVC architecture                                   |
| **Spring Data JPA**         | Repository abstraction and database access                                  |
| **Hibernate 6.x**           | Object-Relational Mapping (ORM) and schema management                       |
| **PostgreSQL 16**           | Relational SQL database engine                                              |
| **Jakarta Bean Validation** | Declarative data validation annotations (`@NotNull`, `@Positive`, `@Email`) |
| **Lombok**                  | Boilerplate reduction for entities                                          |
| **Maven**                   | Dependency management and build automation                                  |
| **JUnit 5 & Mockito**       | Automated unit and integration testing                                      |

---

## Project Structure

```text
FactoryOS
├── .docs/
│   ├── master.md                       # Master project specification
│   └── walkthroughs/                   # Phase walkthroughs and installation guides
│       ├── phase-1-walkthrough.md
│       ├── phase-2-walkthrough.md
│       ├── phase-3-walkthrough.md
│       └── installation-and-guide.md
├── .mvn/wrapper/                       # Maven wrapper binaries and configuration
├── src/
│   ├── main/
│   │   ├── java/com/factoryos/
│   │   │   ├── controller/             # REST Controllers
│   │   │   │   ├── HealthController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── SupplierController.java
│   │   │   │   └── InventoryController.java
│   │   │   ├── dto/                    # Data Transfer Objects (Java Records)
│   │   │   │   ├── CreateProductRequest.java
│   │   │   │   ├── UpdateProductRequest.java
│   │   │   │   ├── ProductResponse.java
│   │   │   │   ├── CreateSupplierRequest.java
│   │   │   │   ├── UpdateSupplierRequest.java
│   │   │   │   ├── SupplierResponse.java
│   │   │   │   ├── StockInRequest.java
│   │   │   │   ├── StockOutRequest.java
│   │   │   │   ├── StockAdjustmentRequest.java
│   │   │   │   ├── InventoryResponse.java
│   │   │   │   ├── StockMovementResponse.java
│   │   │   │   ├── LowStockResponse.java
│   │   │   │   └── HealthResponse.java
│   │   │   ├── entity/                 # JPA Entities
│   │   │   │   ├── Product.java
│   │   │   │   ├── Supplier.java
│   │   │   │   ├── Inventory.java
│   │   │   │   ├── StockMovement.java
│   │   │   │   └── StockMovementType.java
│   │   │   ├── exception/              # Exceptions & Global Exception Handler
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── BusinessRuleException.java
│   │   │   │   ├── InsufficientStockException.java
│   │   │   │   ├── InventoryNotFoundException.java
│   │   │   │   ├── InvalidStockAdjustmentException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── mapper/                 # Entity-DTO Mappers
│   │   │   │   ├── ProductMapper.java
│   │   │   │   ├── SupplierMapper.java
│   │   │   │   ├── InventoryMapper.java
│   │   │   │   └── StockMovementMapper.java
│   │   │   ├── repository/             # Spring Data JPA Repositories
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── SupplierRepository.java
│   │   │   │   ├── InventoryRepository.java
│   │   │   │   └── StockMovementRepository.java
│   │   │   ├── service/                # Business Logic Services
│   │   │   │   ├── ProductService.java & ProductServiceImpl.java
│   │   │   │   ├── SupplierService.java & SupplierServiceImpl.java
│   │   │   │   └── InventoryService.java & InventoryServiceImpl.java
│   │   │   └── FactoryOsApplication.java # Main Application Class
│   │   └── resources/
│   │       └── application.properties  # Application & Database properties
│   └── test/
│       └── java/com/factoryos/
│           ├── controller/             # WebMvc MockMvc Tests
│           │   ├── HealthControllerTest.java
│           │   ├── ProductControllerTest.java
│           │   ├── SupplierControllerTest.java
│           │   └── InventoryControllerTest.java
│           ├── service/                # Service Layer Mockito Unit Tests
│           │   ├── ProductServiceTest.java
│           │   ├── SupplierServiceTest.java
│           │   └── InventoryServiceTest.java
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

### Inventory Management

#### 1. Stock In

- **Method**: `POST`
- **Path**: `/api/inventory/stock-in`
- **Request Body**:

```json
{
  "productId": 1,
  "quantity": 100,
  "reference": "GRN-1001",
  "reason": "Initial stock batch"
}
```

- **Response (`200 OK`)**:

```json
{
  "id": 1,
  "productId": 1,
  "sku": "BRG-6204",
  "productName": "Steel Bearing 6204",
  "quantityAvailable": 100,
  "reservedQuantity": 0,
  "lowStock": false,
  "lastUpdated": "2026-09-11T08:43:58.928236Z"
}
```

#### 2. Stock Out

- **Method**: `POST`
- **Path**: `/api/inventory/stock-out`
- **Request Body**:

```json
{
  "productId": 1,
  "quantity": 85,
  "reference": "PROD-REQ-501",
  "reason": "Assembly line consumption"
}
```

- **Response (`200 OK`)**:

```json
{
  "id": 1,
  "productId": 1,
  "sku": "BRG-6204",
  "productName": "Steel Bearing 6204",
  "quantityAvailable": 15,
  "reservedQuantity": 0,
  "lowStock": true,
  "lastUpdated": "2026-09-11T08:43:58.978503Z"
}
```

#### 3. Stock Out with Insufficient Stock (Negative-Stock Prevention)

- **Method**: `POST`
- **Path**: `/api/inventory/stock-out`
- **Request Body**:

```json
{
  "productId": 1,
  "quantity": 20,
  "reference": "PROD-REQ-502",
  "reason": "Overconsumption attempt"
}
```

- **Response (`409 Conflict`)**:

```json
{
  "timestamp": "2026-09-11T08:43:59.026121Z",
  "status": 409,
  "error": "Insufficient Stock",
  "message": "Cannot remove 20 units of BRG-6204. Available stock: 15",
  "path": "/api/inventory/stock-out"
}
```

#### 4. Stock Adjustment

- **Method**: `POST`
- **Path**: `/api/inventory/adjust`
- **Request Body**:

```json
{
  "productId": 1,
  "newQuantity": 95,
  "reference": "AUDIT-SEPT-2026",
  "reason": "Physical cycle count"
}
```

- **Response (`200 OK`)**:

```json
{
  "id": 1,
  "productId": 1,
  "sku": "BRG-6204",
  "productName": "Steel Bearing 6204",
  "quantityAvailable": 95,
  "reservedQuantity": 0,
  "lowStock": false,
  "lastUpdated": "2026-09-11T08:43:59.000644Z"
}
```

#### 5. Get Low-Stock Products

- **Method**: `GET`
- **Path**: `/api/inventory/low-stock`
- **Response (`200 OK`)**:

```json
[
  {
    "productId": 1,
    "sku": "BRG-6204",
    "productName": "Steel Bearing 6204",
    "quantityAvailable": 15,
    "reorderLevel": 20,
    "lowStock": true
  }
]
```

#### 6. Get Movement History

- **Method**: `GET`
- **Path**: `/api/inventory/{productId}/movements`
- **Response (`200 OK`)**:

```json
[
  {
    "id": 3,
    "productId": 1,
    "movementType": "ADJUSTMENT",
    "quantity": 80,
    "reference": "AUDIT-SEPT-2026",
    "reason": "Physical cycle count",
    "createdAt": "2026-09-11T08:43:59.052389Z"
  },
  {
    "id": 2,
    "productId": 1,
    "movementType": "STOCK_OUT",
    "quantity": 85,
    "reference": "PROD-REQ-501",
    "reason": "Assembly line consumption",
    "createdAt": "2026-09-11T08:43:58.998612Z"
  },
  {
    "id": 1,
    "productId": 1,
    "movementType": "STOCK_IN",
    "quantity": 100,
    "reference": "GRN-1001",
    "reason": "Initial stock batch",
    "createdAt": "2026-09-11T08:43:58.975774Z"
  }
]
```

---

### Purchase Order Management Endpoints

#### 1. Create Purchase Order

- **Method**: `POST`
- **Path**: `/api/purchase-orders`
- **Request Body**:

```json
{
  "supplierId": 2,
  "expectedDeliveryDate": "2026-09-25",
  "items": [
    {
      "productId": 2,
      "quantity": 100,
      "unitPrice": 250.0
    },
    {
      "productId": 3,
      "quantity": 10,
      "unitPrice": 12000.0
    }
  ]
}
```

- **Response (`201 Created`)**:

```json
{
  "id": 1,
  "orderNumber": "PO-000001",
  "supplierId": 2,
  "supplierName": "Apex Hardware Tools",
  "status": "CREATED",
  "orderDate": "2026-09-11",
  "expectedDeliveryDate": "2026-09-25",
  "totalAmount": 145000.0,
  "items": [
    {
      "id": 1,
      "productId": 2,
      "sku": "VLV-1001",
      "productName": "Ball Valve 1/2 Inch",
      "quantity": 100,
      "unitPrice": 250.0,
      "subtotal": 25000.0
    },
    {
      "id": 2,
      "productId": 3,
      "sku": "PMP-3001",
      "productName": "Industrial Water Pump",
      "quantity": 10,
      "unitPrice": 12000.0,
      "subtotal": 120000.0
    }
  ],
  "createdAt": "2026-09-11T10:10:02.453507Z",
  "updatedAt": "2026-09-11T10:10:02.453508Z"
}
```

#### 2. Get All Purchase Orders

- **Method**: `GET`
- **Path**: `/api/purchase-orders`
- **Response (`200 OK`)**:

```json
[
  {
    "id": 1,
    "orderNumber": "PO-000001",
    "supplierId": 2,
    "supplierName": "Apex Hardware Tools",
    "status": "CREATED",
    "orderDate": "2026-09-11",
    "expectedDeliveryDate": "2026-09-25",
    "itemCount": 2,
    "totalAmount": 145000.0,
    "createdAt": "2026-09-11T10:10:02.453507Z"
  }
]
```

#### 3. Get Purchase Order by ID

- **Method**: `GET`
- **Path**: `/api/purchase-orders/{id}`
- **Response (`200 OK`)**: Returns complete `PurchaseOrderResponse` with items and calculated totals.

#### 4. Get Purchase Order by Order Number

- **Method**: `GET`
- **Path**: `/api/purchase-orders/order-number/{orderNumber}`
- **Response (`200 OK`)**: Returns complete `PurchaseOrderResponse`.

#### 5. Approve Purchase Order

- **Method**: `POST`
- **Path**: `/api/purchase-orders/{id}/approve`
- **Preconditions**: Status must be `CREATED`, supplier and all products must be active.
- **Response (`200 OK`)**: Returns updated `PurchaseOrderResponse` with `status: "APPROVED"`.
- **Error (`409 Conflict`)**: If PO is already `APPROVED`, `RECEIVED`, or `CANCELLED`.

#### 6. Receive Purchase Order (Atomic Inventory Receipt)

- **Method**: `POST`
- **Path**: `/api/purchase-orders/{id}/receive`
- **Preconditions**: Status must be `APPROVED`.
- **Side Effects**: Atomic transaction increments `Inventory.quantityAvailable` for all PO items, creates individual `STOCK_IN` movements referencing the PO order number, and transitions status to `RECEIVED`.
- **Response (`200 OK`)**: Returns updated `PurchaseOrderResponse` with `status: "RECEIVED"`.
- **Error (`409 Conflict`)**: If PO is in `CREATED` (not yet approved), already `RECEIVED`, or `CANCELLED`.

#### 7. Cancel Purchase Order

- **Method**: `POST`
- **Path**: `/api/purchase-orders/{id}/cancel`
- **Preconditions**: Status must be `CREATED` or `APPROVED`.
- **Side Effects**: Transitions status to `CANCELLED`. Zero inventory changes, zero stock movements created.
- **Response (`200 OK`)**: Returns updated `PurchaseOrderResponse` with `status: "CANCELLED"`.
- **Error (`409 Conflict`)**: If PO is already `RECEIVED` (cancellation after receipt is strictly prohibited) or already `CANCELLED`.

---

## Example End-to-End Inventory Lifecycle Workflow

The complete end-to-end lifecycle demonstrates how the system maintains accurate stock counts and prevents negative stock:

1. **Step 1 — Create Product**:
   `POST /api/products` with SKU `PMP-3001` and `reorderLevel = 20`.
   - The product is created and an associated `Inventory` record is automatically initialized with `quantityAvailable = 0` (`lowStock: true`).
2. **Step 2 — Stock In**:
   `POST /api/inventory/stock-in` with `quantity = 100`.
   - Inventory becomes `100` (`lowStock: false`). A `STOCK_IN` movement is logged.
3. **Step 3 — Stock Out**:
   `POST /api/inventory/stock-out` with `quantity = 85`.
   - Inventory becomes `15`. Since `15 <= 20`, the product is tagged as `lowStock: true`. A `STOCK_OUT` movement is logged.
4. **Step 4 — Low-Stock Detection**:
   `GET /api/inventory/low-stock` query executes directly against PostgreSQL.
   - Product `PMP-3001` appears in the low-stock report.
5. **Step 5 — Stock Out Rejection**:
   `POST /api/inventory/stock-out` with `quantity = 20`.
   - Request is rejected with `409 Conflict`. Stock remains `15`. No movement record is created.
6. **Step 6 — Stock Adjustment**:
   `POST /api/inventory/adjust` with `newQuantity = 95`.
   - Inventory balance updates to `95`. An `ADJUSTMENT` movement is recorded with delta `+80`.
7. **Step 7 — Procurement Order Created & Approved (Phase 5)**:
   - Create PO: `POST /api/purchase-orders` for 20 units of `PMP-3001`. Status is `CREATED`. Inventory remains `95`.
   - Direct receive attempt is rejected: `POST /api/purchase-orders/{id}/receive` returns `409 Conflict` ("Only APPROVED purchase orders can be received").
   - Approve PO: `POST /api/purchase-orders/{id}/approve`. Status becomes `APPROVED`. Inventory still remains `95`.
8. **Step 8 — Atomic Goods Receipt (Phase 5)**:
   - Receive PO: `POST /api/purchase-orders/{id}/receive`.
   - Inventory atomically increases: `95 + 20 = 115`.
   - A `STOCK_IN` movement is logged referencing `PO-000003` with reason `"Purchase order received"`.
   - PO status becomes `RECEIVED`. Duplicate receive or cancellation attempts return `409 Conflict`.

---

## Error Handling Architecture & Standards

FactoryOS employs an enterprise-grade validation and exception-handling strategy guaranteeing consistent, predictable error responses across all APIs:

```text
HTTP Request
     ↓
DTO & Path Validation (Jakarta @Valid, @Validated)
     ↓ [Failure: MethodArgumentNotValidException / ConstraintViolationException]
Controller Layer
     ↓
Service Layer (Business Rules & Domain Invariants)
     ↓ [Failure: ApplicationException subclass]
Database / Hibernate Layer
     ↓ [Failure: DataIntegrityViolationException / OptimisticLockException]
Centralized Handler (@RestControllerAdvice)
     ↓
Consistent JSON ErrorResponse (No Stack Traces, No Internal SQL Leakage)
```

### HTTP Status Code Mapping

| HTTP Status                     | Category                       | Typical Causes                                                                                                                                                                                                                     |
| :------------------------------ | :----------------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`400 Bad Request`**           | Input / Validation / Malformed | Field validation failure, constraint violation, malformed JSON body, unparseable data types, missing required query parameters, or business rule conflicts.                                                                        |
| **`404 Not Found`**             | Resource Missing               | Nonexistent product, supplier, inventory record, purchase order ID, or unmapped URL path.                                                                                                                                          |
| **`405 Method Not Allowed`**    | Invalid HTTP Method            | Using an unsupported HTTP verb (e.g. `PATCH` on a resource that only supports `GET`/`PUT`).                                                                                                                                        |
| **`409 Conflict`**              | State & Integrity Conflict     | Duplicate SKU, insufficient available stock on stock-out, illegal purchase order state transitions, duplicate receipt, cancellation of received orders, concurrent modification (optimistic lock), or database unique constraints. |
| **`500 Internal Server Error`** | Server-Side Exception          | Unhandled runtime errors. Logged with full stack traces on the server; client receives a sanitized response with zero internal leakage.                                                                                            |

### Standard JSON Error Envelope

Every error response adheres to the `ErrorResponse` schema:

```json
{
  "timestamp": "2026-09-11T16:10:58.696647Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed for one or more fields",
  "path": "/api/products",
  "errors": {
    "name": "Product name cannot be blank",
    "unitPrice": "Unit price must be greater than or equal to 0"
  },
  "validationErrors": {
    "name": "Product name cannot be blank",
    "unitPrice": "Unit price must be greater than or equal to 0"
  }
}
```

### Error Response Examples

#### 1. Validation Failure (`400 Bad Request`)

```json
{
  "timestamp": "2026-09-11T16:11:23.134043Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed for one or more fields",
  "path": "/api/purchase-orders",
  "errors": {
    "items[0].quantity": "Quantity must be greater than 0",
    "items[0].unitPrice": "Unit price must be greater than or equal to 0"
  }
}
```

#### 2. Malformed Request Body (`400 Bad Request`)

```json
{
  "timestamp": "2026-09-11T16:10:39.424452Z",
  "status": 400,
  "error": "Malformed Request",
  "message": "Malformed JSON request body or invalid data type format",
  "path": "/api/products"
}
```

#### 3. Resource Not Found (`404 Not Found`)

```json
{
  "timestamp": "2026-09-11T16:11:06.946156Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 99999",
  "path": "/api/products/99999"
}
```

#### 4. Insufficient Stock Conflict (`409 Conflict`)

```json
{
  "timestamp": "2026-09-11T16:11:14.515347Z",
  "status": 409,
  "error": "Insufficient Stock",
  "message": "Cannot remove 999999 units of PMP-3001. Available stock: 115",
  "path": "/api/inventory/stock-out"
}
```

#### 5. Invalid Purchase Order State Transition (`409 Conflict`)

```json
{
  "timestamp": "2026-09-11T16:11:26.668292Z",
  "status": 409,
  "error": "Invalid Purchase Order State",
  "message": "Cannot receive purchase order PO-000004 because it has been cancelled",
  "path": "/api/purchase-orders/4/receive"
}
```

---

## Automated Test Suite & Business Rules Matrix

FactoryOS maintains a multi-tiered test pyramid ensuring business logic integrity across all architectural layers. The automated suite contains **116 automated tests** running with 0 failures and 0 errors.

| Test Layer       | Test Class                              | Business Rule Tested                                                                   | Assertion Type                            | Sample Test Name                                                                   |
| :--------------- | :-------------------------------------- | :------------------------------------------------------------------------------------- | :---------------------------------------- | :--------------------------------------------------------------------------------- |
| **Service Unit** | `InventoryServiceTest`                  | Negative stock prevention (`Stock-Out`)                                                | Exception + State Verification            | `stockOut_insufficientStock_throwsExceptionAndDoesNotSave`                         |
| **Service Unit** | `InventoryServiceTest`                  | Inactive product inventory rejection                                                   | Exception Verification                    | `stockIn_inactiveProduct_throwsBusinessRuleException`                              |
| **Service Unit** | `InventoryServiceTest`                  | Low stock reorder threshold boundaries                                                 | Boolean Assertion                         | `getLowStockItems_atReorderLevel_marksLowStockTrue`                                |
| **Service Unit** | `InventoryServiceTest`                  | Zero/negative quantity adjustment                                                      | Exception Verification                    | `adjustStock_zeroOrNegativeQuantity_throwsIllegalArgumentException`                |
| **Service Unit** | `PurchaseOrderServiceTest`              | Strict status transitions (`CREATED -> APPROVED -> RECEIVED`)                          | Enum & State Verification                 | `approvePurchaseOrder_validState_updatesStatus`                                    |
| **Service Unit** | `PurchaseOrderServiceTest`              | Terminal state immutability (`RECEIVED`/`CANCELLED`)                                   | Exception Verification                    | `receivePurchaseOrder_alreadyReceived_throwsInvalidStateException`                 |
| **Service Unit** | `PurchaseOrderServiceTest`              | Purchase order atomic receipt & movement creation                                      | Repository Argument Captor                | `receivePurchaseOrder_approvedOrder_incrementsInventoryAndCreatesMovement`         |
| **Service Unit** | `PurchaseOrderServiceTest`              | Sequential order number generation (`PO-000001`)                                       | String Format Assertion                   | `createPurchaseOrder_generatesSequentialOrderNumber`                               |
| **Service Unit** | `PurchaseOrderServiceTest`              | Duplicate line-item product rejection                                                  | Exception Verification                    | `createPurchaseOrder_duplicateProductInItems_throwsException`                      |
| **Service Unit** | `ProductServiceTest`                    | SKU uniqueness validation                                                              | Exception Verification                    | `createProduct_duplicateSku_throwsDuplicateResourceException`                      |
| **Service Unit** | `SupplierServiceTest`                   | Supplier email uniqueness                                                              | Exception Verification                    | `createSupplier_duplicateEmail_throwsDuplicateResourceException`                   |
| **WebMvc Slice** | `CorsConfigTest`                        | Preflight CORS origin filtering and unauthorized rejection                             | HTTP Status + Access-Control Headers      | `cors_shouldAllowConfiguredDevOrigin`                                              |
| **WebMvc Slice** | `FactoryOsPropertiesTest`               | Configuration property binding and default fallback validation                         | AssertJ Context Assertions                | `shouldBindDefaultAllowedOrigins`                                                  |
| **WebMvc Slice** | `InventoryControllerTest`               | Request body validation (`quantity > 0`, `@NotNull`)                                   | HTTP 400 + JSON Error Fields              | `stockIn_zeroQuantity_returnsBadRequest`                                           |
| **WebMvc Slice** | `PurchaseOrderControllerTest`           | Nested collection item validation (`items[0].quantity <= 0`)                           | HTTP 400 + Nested JSON Paths              | `createPurchaseOrder_invalidItemQuantityAndPrice_returnsBadRequestWithFieldErrors` |
| **WebMvc Slice** | `GlobalExceptionHandlerTest`            | Domain exception to HTTP code translations                                             | HTTP Status + ErrorResponse Envelope      | `handleInsufficientStockException_returnsConflictResponse`                         |
| **WebMvc Slice** | `GlobalExceptionHandlerTest`            | Internal server error sanitization (no SQL leaks)                                      | Generic Error Message Assertion           | `handleGenericException_returnsInternalServerErrorResponse`                        |
| **WebMvc Slice** | `OpenApiConfigTest`                     | OpenAPI specification generation (`/v3/api-docs`)                                      | JSON Spec Metadata Assertions             | `apiDocsEndpointReturnsMetadata`                                                   |
| **WebMvc Slice** | `OpenApiConfigTest`                     | Swagger UI endpoint accessibility                                                      | HTTP 3xx Redirection                      | `swaggerUiEndpointIsAccessible`                                                    |
| **Integration**  | `ProductionHardeningIntegrationTest`    | Optimistic locking collision prevention on concurrent inventory updates                | `ObjectOptimisticLockingFailureException` | `optimisticLocking_concurrentUpdate_throwsConflict`                                |
| **Integration**  | `ProductionHardeningIntegrationTest`    | Database constraint enforcement (duplicate SKU uniqueness)                             | `DataIntegrityViolationException`         | `duplicateSku_violatesUniqueConstraint`                                            |
| **Integration**  | `DevelopmentDataInitializerTest`        | Realistic multi-tier demo data initialization                                          | Repository Count Assertions               | `seedsProductsAndSuppliers`                                                        |
| **Integration**  | `DevelopmentDataInitializerTest`        | Seed idempotency (no duplicate records on restarts)                                    | Idempotent Run State Assertions           | `seedingIsIdempotent`                                                              |
| **Integration**  | `PurchaseOrderReceivingIntegrationTest` | End-to-end receipt updates inventory, movements, and PO status                         | Database State Assertions                 | `receivePurchaseOrder_successful_updatesInventoryAndMovementsAndStatus`            |
| **Integration**  | `PurchaseOrderReceivingIntegrationTest` | **Atomic rollback proof**: fails midway, rolls back all previous stock & audit records | Strict Database Pre/Post Assertions       | `receivePurchaseOrder_whenItemFails_rollsBackEntireTransactionAtomically`          |
| **Integration**  | `InventoryIntegrationTest`              | Database-level unique SKU constraint enforcement                                       | `DataIntegrityViolationException`         | `duplicateSku_violatesDatabaseUniqueConstraint`                                    |
| **Integration**  | `InventoryIntegrationTest`              | Real database stock lifecycle (stock-in, stock-out, boundary checks)                   | Direct Database Queries                   | `inventoryLifecycle_stockInStockOutAndNegativeStockRejection`                      |

### Code Coverage (JaCoCo)

FactoryOS utilizes `jacoco-maven-plugin` (0.8.12) to verify test execution depth.

| Package                      | Instruction Coverage | Branch Coverage | Classes Analyzed |
| :--------------------------- | :------------------- | :-------------- | :--------------- |
| `com.factoryos.config`       | **100%**             | **90%**         | 5                |
| `com.factoryos.dto`          | **100%**             | N/A             | 18               |
| `com.factoryos.controller`   | **89%**              | N/A             | 5                |
| `com.factoryos.service`      | **89%**              | **66%**         | 3                |
| `com.factoryos.exception`    | **84%**              | **55%**         | 10               |
| `com.factoryos.entity`       | **79%**              | **50%**         | 7                |
| `com.factoryos.service.impl` | **74%**              | **71%**         | 1                |
| `com.factoryos.mapper`       | **74%**              | **42%**         | 5                |
| **Total Project**            | **86%**              | **57%**         | **55**           |

To generate the HTML coverage report locally:

```bash
./mvnw test jacoco:report
```

The report is saved to `target/site/jacoco/index.html`.

---

## Production Hardening & Configuration Guide

FactoryOS is engineered to cleanly separate development conveniences from strict production security and data integrity requirements.

### Environment Variable Matrix

All operational parameters are externalized from compiled source code and can be injected via standard environment variables or container secrets:

| Variable                 | Description                                   | Default (Dev)                                | Production Recommendation                                             |
| :----------------------- | :-------------------------------------------- | :------------------------------------------- | :-------------------------------------------------------------------- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`dev`, `demo`, `prod`) | `dev`                                        | Set to `prod`                                                         |
| `SERVER_PORT`            | HTTP server listening port                    | `8080`                                       | Container-specific port or `8080`                                     |
| `DB_URL`                 | JDBC URL for PostgreSQL database              | `jdbc:postgresql://localhost:5432/factoryos` | Production RDS/Cloud SQL JDBC endpoint with TLS                       |
| `DB_USERNAME`            | Database connection username                  | `postgres`                                   | Least-privilege application user                                      |
| `DB_PASSWORD`            | Database connection password                  | `postgres`                                   | Secure secret managed via vault/secrets manager                       |
| `JPA_DDL_AUTO`           | Hibernate DDL lifecycle strategy              | `update` (dev)                               | `validate` (schema managed via controlled migrations)                 |
| `CORS_ALLOWED_ORIGINS`   | Permitted browser origins for API             | `http://localhost:5173`                      | Exact production frontend URL (e.g., `https://factoryos.company.com`) |
| `SWAGGER_ENABLED`        | Toggle OpenAPI docs and Swagger UI            | `true` (dev)                                 | `false` (or restricted via gateway)                                   |

### Development vs. Production Profile Separation

1. **Development Profile (`dev`)**:
   - Automatically active by default (`spring.profiles.default=dev`).
   - `spring.jpa.hibernate.ddl-auto=update` allows rapid iterative development.
   - `DevelopmentDataInitializer` automatically runs idempotently to populate rich seed datasets.
   - Swagger UI and OpenAPI documentation are enabled at `/swagger-ui/index.html`.
   - Permissive local CORS defaults to `http://localhost:5173`.

2. **Production Profile (`prod`)**:
   - Activated via `SPRING_PROFILES_ACTIVE=prod`.
   - Configured in `src/main/resources/application-prod.properties`.
   - `spring.jpa.hibernate.ddl-auto=validate`: **Guarantees the application never silently modifies the database schema at startup.** Any discrepancy between JPA entities and database tables triggers a fast, fail-safe application startup abort.
   - `DevelopmentDataInitializer` is **disabled** (`@Profile({"dev", "demo"})`), ensuring zero demo records are seeded into production.
   - Swagger UI is **disabled** by default (`SWAGGER_ENABLED=false`) to protect API surface introspection.
   - SQL query logging is disabled (`show-sql=false`, `logging.level.org.hibernate.SQL=WARN`) to eliminate performance overhead and prevent parameter leakage.

### Database Constraints & Invariant Enforcement

FactoryOS enforces critical industrial invariants directly in the database engine using PostgreSQL check constraints in addition to application-layer Bean Validation:

- **Products**: `unit_price >= 0 AND reorder_level >= 0` enforced via `@Check` and `uk_product_sku` unique constraint.
- **Inventory**: `quantity_available >= 0 AND reserved_quantity >= 0` enforced via `@Check` and `uk_inventory_product_id` unique constraint.
- **Purchase Orders**: `total_amount >= 0` enforced via `@Check` and `uk_purchase_order_number` unique constraint.
- **Purchase Order Items**: `quantity > 0 AND unit_price >= 0 AND subtotal >= 0` enforced via `@Check`.

### Concurrency & Optimistic Locking

To protect inventory and procurement states against race conditions and concurrent write collisions:

- `Inventory` entity is guarded by a `@Version` counter.
- `PurchaseOrder` entity is guarded by a `@Version` counter.
- When concurrent operations attempt conflicting updates, the stale transaction throws `ObjectOptimisticLockingFailureException`, which `GlobalExceptionHandler` translates into a structured `409 Conflict` ("Optimistic Lock Conflict").

### Audit Logging & Secret Protection

- Structured SLF4J logging across all services (`InventoryServiceImpl`, `ProductServiceImpl`, `SupplierServiceImpl`, `PurchaseOrderServiceImpl`).
- High-signal operational records for all stock-in, stock-out, stock adjustment, product deactivation, and PO lifecycle state changes.
- **Zero Sensitive Data In Logs**: No passwords, tokens, full request bodies, or internal database exception details are logged or exposed to clients.
- `.gitignore` explicitly prevents `.env` and `.env.*` files from being committed, while `.env.example` provides an audited template.

---

## Interactive Swagger & OpenAPI Documentation

FactoryOS integrates OpenAPI 3.1 and Swagger UI using `springdoc-openapi-starter-webmvc-ui` (v2.8.5). When the backend application is running, developers, interviewers, and API evaluators can access the interactive documentation directly in their browser:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI 3.1 JSON Specification**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### API Groups & Domain Organization

The REST endpoints are organized into 5 logical business tags:

1. **Products (`/api/products`)**:
   - Product catalog registration, SKU uniqueness guarantees, pricing updates, and soft deactivation.
2. **Suppliers (`/api/suppliers`)**:
   - Supplier directory management, unique email validation, and active status tracking.
3. **Inventory (`/api/inventory`)**:
   - Real-time stock queries, atomic stock-in/stock-out operations, non-negative stock enforcement, audit adjustments, and low-stock alerts.
4. **Purchase Orders (`/api/purchase-orders`)**:
   - Procurement order creation, line items snapshotting, sequential order number generation (`PO-000001`), authoritative financial totals, and lifecycle state transitions (`CREATED -> APPROVED -> RECEIVED / CANCELLED`).
5. **System (`/api/health`)**:
   - Health check and service readiness verification.

### Business Rules Embedded in Documentation

The OpenAPI contract details both technical specifications and domain rules:

- **Negative Stock Prevention**: Documents that `POST /api/inventory/stock-out` enforces `quantityAvailable >= quantity` and returns `409 Conflict` (`InsufficientStockException`) if stock would drop below zero.
- **Transactional Goods Receipt**: Documents that `POST /api/purchase-orders/{id}/receive` executes in an atomic `@Transactional` boundary, incrementing stock for each line item, logging `STOCK_IN` movements, and setting status to `RECEIVED` with complete rollback protection if any item fails.
- **Purchase Order State Machine**: Documents allowed transitions (`CREATED -> APPROVED`, `APPROVED -> RECEIVED`, `CREATED/APPROVED -> CANCELLED`) and terminal state immutability.
- **Error Envelopes**: Documents `400 Bad Request`, `404 Not Found`, `409 Conflict`, and `500 Internal Server Error` using the standardized `ErrorResponse` schema.

---

## Development Demo Data & Seeding Strategy

FactoryOS includes an environment-aware, idempotent development data seeder (`DevelopmentDataInitializer`) designed to make the application immediately demonstrable on startup without manual data entry.

### Activation & Profile Isolation

The demo data seeder is bound to the `dev` and `demo` Spring profiles (`@Profile({"dev", "demo"})`):

- In development mode (configured as default via `spring.profiles.default=dev`), demo data is automatically populated on first run.
- Production environments (`SPRING_PROFILES_ACTIVE=prod`) never activate this component, guaranteeing zero accidental test records in production.

### Seed Dataset Overview

1. **8 Realistic Industrial Products**:
   - `MOTOR-001` (Industrial Electric Motor, ₹18,500.00, reorder level 10)
   - `BEARING-001` (Heavy Duty Ball Bearing, ₹450.00, reorder level 25)
   - `BELT-001` (Reinforced Conveyor Belt, ₹1,200.00, reorder level 15)
   - `GEAR-001` (Steel Gear Assembly, ₹3,400.00, reorder level 8)
   - `PUMP-001` (Hydraulic Gear Pump, ₹8,200.00, reorder level 5)
   - `VALVE-001` (Industrial Control Valve, ₹2,650.00, reorder level 12)
   - `SENSOR-001` (RTD Temperature Sensor, ₹950.00, reorder level 20)
   - `FILTER-001` (High-Pressure Hydraulic Filter, ₹380.00, reorder level 30)

2. **5 Realistic Suppliers**:
   - `ABC Industrial Supplies` (Kolkata, West Bengal)
   - `Eastern Engineering Components` (Howrah, West Bengal)
   - `Metro Machine Parts` (Jamshedpur, Jharkhand)
   - `National Hydraulic Systems` (Pune, Maharashtra)
   - `Precision Automation Ltd.` (Bengaluru, Karnataka)

3. **Multi-Tier Inventory Distribution**:
   - **Healthy Stock**: `MOTOR-001` (45 available), `BEARING-001` (120 available), `BELT-001` (35 available), `SENSOR-001` (25 available), `FILTER-001` (50 available).
   - **Low Stock (Triggering Alerts)**: `PUMP-001` (3 available vs reorder 5), `VALVE-001` (8 available vs reorder 12).
   - **Zero Stock**: `GEAR-001` (0 available vs reorder 8).

4. **Realistic Audit Trail**:
   - Seeded `STOCK_IN`, `STOCK_OUT`, and `ADJUSTMENT` movement records reflecting initial deliveries, production usage, and cycle count reconciliations.

5. **4 Purchase Orders Across All Lifecycle States**:
   - `PO-000001` (`CREATED`): Draft order from ABC Industrial Supplies (Motors + Gears), ready for approval.
   - `PO-000002` (`APPROVED`): Authorized order from Eastern Engineering Components (Bearings + Belts), ready for goods receipt.
   - `PO-000003` (`RECEIVED`): Fully received historical procurement order from Metro Machine Parts (Filters + Sensors).
   - `PO-000004` (`CANCELLED`): Terminated order from Precision Automation Ltd. (Valves).

### Idempotency Guarantee

The initializer executes an idempotency check (`productRepository.existsBySku("MOTOR-001")`) before executing any database operations. If demo data already exists from a previous run, seeding is immediately skipped. The application can restart hundreds of times without creating duplicate records or causing constraint errors.

---

## End-to-End Demonstration Workflow

Follow this 5-minute walkthrough to demonstrate FactoryOS capabilities:

```text
1. Start Backend & Open Swagger UI
   └─ Run: ./mvnw spring-boot:run
   └─ Navigate to: http://localhost:8080/swagger-ui/index.html

2. Explore Low Stock Alerts
   └─ Execute: GET /api/inventory/low-stock
   └─ Observe: PUMP-001, VALVE-001, and GEAR-001 returned with lowStock = true.

3. Inspect Pre-seeded Purchase Orders
   └─ Execute: GET /api/purchase-orders
   └─ Observe: POs in CREATED, APPROVED, RECEIVED, and CANCELLED states.

4. Walk Through the Complete Procurement Lifecycle:
   a. Look up PO-000001 (Status: CREATED) via GET /api/purchase-orders/order-number/PO-000001
   b. Approve the order via POST /api/purchase-orders/{id}/approve
      └─ Status updates from CREATED to APPROVED
   c. Receive goods via POST /api/purchase-orders/{id}/receive
      └─ Status updates to RECEIVED
      └─ Inventory for MOTOR-001 increases (45 -> 50)
      └─ Inventory for GEAR-001 increases (0 -> 8, clearing zero stock)
      └─ STOCK_IN movement records are generated atomically
   d. Query GET /api/inventory/low-stock
      └─ GEAR-001 is now restocked and cleared from zero-stock condition!
```

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

### 3. Start Backend Application

```bash
./mvnw spring-boot:run
```

The Spring Boot backend starts on port `8080` (`http://localhost:8080`).

### 4. Start Frontend Dashboard

In a new terminal window:

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

The Vite development server starts on port `5173` (`http://localhost:5173`).
