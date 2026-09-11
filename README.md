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

### Currently Implemented (Phases 1, 2, 3, 3.5, 4, 5, 6 & 7)

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

FactoryOS maintains a multi-tiered test pyramid ensuring business logic integrity across all architectural layers. The automated suite contains **103 automated tests** running in ~18 seconds with 0 failures and 0 errors.

| Test Layer | Test Class | Business Rule Tested | Assertion Type | Sample Test Name |
| :--- | :--- | :--- | :--- | :--- |
| **Service Unit** | `InventoryServiceTest` | Negative stock prevention (`Stock-Out`) | Exception + State Verification | `stockOut_insufficientStock_throwsExceptionAndDoesNotSave` |
| **Service Unit** | `InventoryServiceTest` | Inactive product inventory rejection | Exception Verification | `stockIn_inactiveProduct_throwsBusinessRuleException` |
| **Service Unit** | `InventoryServiceTest` | Low stock reorder threshold boundaries | Boolean Assertion | `getLowStockItems_atReorderLevel_marksLowStockTrue` |
| **Service Unit** | `InventoryServiceTest` | Zero/negative quantity adjustment | Exception Verification | `adjustStock_zeroOrNegativeQuantity_throwsIllegalArgumentException` |
| **Service Unit** | `PurchaseOrderServiceTest` | Strict status transitions (`CREATED -> APPROVED -> RECEIVED`) | Enum & State Verification | `approvePurchaseOrder_validState_updatesStatus` |
| **Service Unit** | `PurchaseOrderServiceTest` | Terminal state immutability (`RECEIVED`/`CANCELLED`) | Exception Verification | `receivePurchaseOrder_alreadyReceived_throwsInvalidStateException` |
| **Service Unit** | `PurchaseOrderServiceTest` | Purchase order atomic receipt & movement creation | Repository Argument Captor | `receivePurchaseOrder_approvedOrder_incrementsInventoryAndCreatesMovement` |
| **Service Unit** | `PurchaseOrderServiceTest` | Sequential order number generation (`PO-000001`) | String Format Assertion | `createPurchaseOrder_generatesSequentialOrderNumber` |
| **Service Unit** | `PurchaseOrderServiceTest` | Duplicate line-item product rejection | Exception Verification | `createPurchaseOrder_duplicateProductInItems_throwsException` |
| **Service Unit** | `ProductServiceTest` | SKU uniqueness validation | Exception Verification | `createProduct_duplicateSku_throwsDuplicateResourceException` |
| **Service Unit** | `SupplierServiceTest` | Supplier email uniqueness | Exception Verification | `createSupplier_duplicateEmail_throwsDuplicateResourceException` |
| **WebMvc Slice** | `InventoryControllerTest` | Request body validation (`quantity > 0`, `@NotNull`) | HTTP 400 + JSON Error Fields | `stockIn_zeroQuantity_returnsBadRequest` |
| **WebMvc Slice** | `PurchaseOrderControllerTest` | Nested collection item validation (`items[0].quantity <= 0`) | HTTP 400 + Nested JSON Paths | `createPurchaseOrder_invalidItemQuantityAndPrice_returnsBadRequestWithFieldErrors` |
| **WebMvc Slice** | `GlobalExceptionHandlerTest` | Domain exception to HTTP code translations | HTTP Status + ErrorResponse Envelope | `handleInsufficientStockException_returnsConflictResponse` |
| **WebMvc Slice** | `GlobalExceptionHandlerTest` | Internal server error sanitization (no SQL leaks) | Generic Error Message Assertion | `handleGenericException_returnsInternalServerErrorResponse` |
| **Integration** | `PurchaseOrderReceivingIntegrationTest` | End-to-end receipt updates inventory, movements, and PO status | Database State Assertions | `receivePurchaseOrder_successful_updatesInventoryAndMovementsAndStatus` |
| **Integration** | `PurchaseOrderReceivingIntegrationTest` | **Atomic rollback proof**: fails midway, rolls back all previous stock & audit records | Strict Database Pre/Post Assertions | `receivePurchaseOrder_whenItemFails_rollsBackEntireTransactionAtomically` |
| **Integration** | `InventoryIntegrationTest` | Database-level unique SKU constraint enforcement | `DataIntegrityViolationException` | `duplicateSku_violatesDatabaseUniqueConstraint` |
| **Integration** | `InventoryIntegrationTest` | Real database stock lifecycle (stock-in, stock-out, boundary checks) | Direct Database Queries | `inventoryLifecycle_stockInStockOutAndNegativeStockRejection` |

### Code Coverage (JaCoCo)

FactoryOS utilizes `jacoco-maven-plugin` (0.8.12) to verify test execution depth.

| Package | Instruction Coverage | Branch Coverage | Classes Analyzed |
| :--- | :--- | :--- | :--- |
| `com.factoryos.controller` | **89%** | N/A | 5 |
| `com.factoryos.service` | **87%** | **66%** | 3 |
| `com.factoryos.exception` | **84%** | **55%** | 10 |
| `com.factoryos.dto` | **100%** | N/A | 18 |
| `com.factoryos.config` | **100%** | N/A | 1 |
| `com.factoryos.entity` | **79%** | **50%** | 7 |
| **Total Project** | **83%** (2,650 / 3,190) | **56%** | **51** |

To generate the HTML coverage report locally:
```bash
./mvnw test jacoco:report
```
The report is saved to `target/site/jacoco/index.html`.

---

## Technical Interview Q&A

### Q1: How do you design service-level unit tests for critical inventory operations?

**Answer:**
When testing mission-critical inventory services, tests must focus on **domain rules and financial boundary conditions**, rather than shallow getter/setter assertions. Key practices include:
1. **Testing Boundary Limits**: Verifying stock balance changes at exact thresholds — such as available quantity dropping to `0` (legal stock-out) vs `-1` (illegal; must trigger `InsufficientStockException`), and available stock equal to `reorderLevel` vs `reorderLevel + 1` for low-stock alerts.
2. **Defensive Precondition Checks**: Testing that operations on inactive products or disabled suppliers throw domain exceptions (`BusinessRuleException`) *before* any repository mutation occurs.
3. **Verifying Side Effects & Audit Trails**: Using Mockito `ArgumentCaptor` to inspect not only that `inventoryRepository.save()` was invoked with the exact mathematical balance, but also that `stockMovementRepository.save()` received a movement entity with the correct movement type (`STOCK_IN`, `STOCK_OUT`, `ADJUSTMENT`), delta, reference type, and previous-vs-new balance snapshots.
4. **Negative Scenario Assertion**: Testing that when a validation or balance check fails, repositories are never touched (`verify(..., never()).save(...)`), guaranteeing no uncommitted entity mutations leak into persistence context.

### Q2: What are the trade-offs between Mockito unit tests, MockMvc slice tests, and Spring Boot integration tests?

**Answer:**
A production-grade system balances speed, isolation, and confidence across three distinct test layers:
- **Mockito Unit Tests (`@ExtendWith(MockitoExtension.class)`)**:
  - *Pros*: Extremely fast (milliseconds), fully isolated, perfect for exhaustive combinatorial business logic, edge cases, and arithmetic invariants.
  - *Cons*: Ignores framework wiring, JPA entity mappings, database constraints, Bean Validation annotations, and transaction boundaries.
- **MockMvc Slice Tests (`@WebMvcTest`)**:
  - *Pros*: Tests the web layer in isolation without booting the persistence layer or database. Validates HTTP routes, status codes, JSON serialization/deserialization, Jakarta validation constraints (`@Valid`, `@NotNull`, `@Min`), and `@RestControllerAdvice` exception translations.
  - *Cons*: Mocks out the service layer; does not verify SQL queries, ORM cascades, database check constraints, or actual transactional atomicity.
- **Spring Boot Integration Tests (`@SpringBootTest` with live database)**:
  - *Pros*: Maximum confidence. Tests full application context, Hibernate SQL generation, PostgreSQL foreign keys, unique indices, and `@Transactional` rollbacks.
  - *Cons*: Slower startup time and requires clean fixture management (e.g., executing `deleteAllInBatch()` in reverse foreign key order) to avoid cross-test database pollution.

### Q3: How do you prove that an inventory transaction actually rolled back on database errors?

**Answer:**
You cannot prove `@Transactional` rollback using unit tests with mocks, because mocks do not participate in a real database transaction. To prove transaction rollback:
1. **Use a Real Database Environment**: Run a `@SpringBootTest` test connecting to a real PostgreSQL instance (not an in-memory database that might lack PostgreSQL-specific constraint behaviors).
2. **Setup Multi-Step Workflows**: Construct a business scenario with multiple persistent mutations. In FactoryOS, `receivePurchaseOrder` receives a PO with multiple items. Processing Item 1 increments Product 1's inventory and inserts a `STOCK_IN` movement. Item 2 is intentionally configured with an invalid condition (e.g., an inactive product or constraint violation) that throws a runtime `BusinessRuleException` midway through the loop.
3. **Execute Outside Test-Managed Transactions**: Do **not** annotate the test method with `@Transactional` (which would auto-rollback the entire test method and mask whether the service method's transaction rolled back).
4. **Assert Database State Post-Failure**:
   - Verify that the service method threw the expected exception.
   - Query the database directly via repositories:
     - Verify Product 1's inventory quantity remains at its original value (e.g., exactly 100, not 150).
     - Verify Product 2's inventory quantity remains untouched.
     - Verify `stockMovementRepository.count()` is `0` (the movement created for Item 1 was completely rolled back).
     - Verify the purchase order status remains `APPROVED`, not `RECEIVED`.
This conclusively proves that the transaction boundary preserved the ACID atomicity guarantee: all mutations succeeded together or all were reverted.

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
