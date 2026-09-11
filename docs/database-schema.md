# FactoryOS — Database Design & Entity Relationships

## 1. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    Product ||--|| Inventory : "has current balance"
    Product ||--o{ StockMovement : "audits changes"
    Supplier ||--o{ PurchaseOrder : "fulfills"
    PurchaseOrder ||--|{ PurchaseOrderItem : "contains line items"
    Product ||--o{ PurchaseOrderItem : "ordered via"

    Product {
        bigint id PK
        varchar(50) sku UK "Unique uppercase SKU"
        varchar(150) name "Product display name"
        varchar(500) description "Specifications"
        varchar(100) category "Product category"
        numeric(12_2) unit_price "Check: >= 0"
        integer reorder_level "Check: >= 0"
        boolean active "Soft-delete flag"
        timestamp created_at
        timestamp updated_at
    }

    Inventory {
        bigint id PK
        bigint product_id FK,UK "1:1 with Product"
        integer quantity_available "Check: >= 0"
        integer reserved_quantity "Check: >= 0"
        bigint version "Optimistic locking counter"
        timestamp last_updated
    }

    StockMovement {
        bigint id PK
        bigint product_id FK "References Product"
        varchar(20) movement_type "STOCK_IN, STOCK_OUT, ADJUSTMENT"
        integer quantity "Delta units moved"
        varchar(100) reference "Document or order number"
        varchar(255) reason "Operational justification"
        timestamp created_at "Audit timestamp"
    }

    Supplier {
        bigint id PK
        varchar(150) name "Supplier company name"
        varchar(100) contact_person
        varchar(150) email
        varchar(30) phone
        varchar(255) address
        boolean active "Soft-delete flag"
        timestamp created_at
        timestamp updated_at
    }

    PurchaseOrder {
        bigint id PK
        varchar(50) order_number UK "Sequential PO-000001"
        bigint supplier_id FK "References Supplier"
        varchar(30) status "CREATED, APPROVED, RECEIVED, CANCELLED"
        date order_date
        date expected_delivery_date
        numeric(12_2) total_amount "Check: >= 0"
        bigint version "Optimistic locking counter"
        timestamp created_at
        timestamp updated_at
    }

    PurchaseOrderItem {
        bigint id PK
        bigint purchase_order_id FK "References PurchaseOrder"
        bigint product_id FK "References Product"
        integer quantity "Check: > 0"
        numeric(12_2) unit_price "Check: >= 0"
        numeric(12_2) subtotal "Check: >= 0"
    }
```

---

## 2. Relational Design Rationale

### 1. Separation of `Product` and `Inventory`

- **Product Master Data**: Contains descriptive, slowly-changing catalog attributes (name, category, description, reorder thresholds, catalog price).
- **Inventory Balance**: Tracks volatile physical stock counts that change multiple times per day.
- **Why Separate?**: This enforces separation of concerns. Multiple warehouse locations or future multi-tenant inventory extensions can reference the same immutable `Product` master entity without altering the core catalog schema.

### 2. Separation of `Inventory` and `StockMovement`

- `Inventory` stores the **current materialized balance** (`quantity_available`). This allows O(1) instantaneous stock checks and low-stock queries.
- `StockMovement` stores an **append-only, immutable transaction log**. Rows are never updated or deleted; they record historical accountability for every unit increment, consumption, or physical count adjustment.

### 3. Junction Entity: `PurchaseOrderItem`

- A single purchase order can contain multiple distinct products, and a single product can be purchased across many orders over time (Many-to-Many).
- `PurchaseOrderItem` acts as the relational associative entity, capturing line-specific attributes:
  - `quantity`: Number of units ordered.
  - `unit_price`: The agreed price _at the moment of order placement_ (historical snapshot, protecting order totals if the product's catalog price changes later).
  - `subtotal`: Explicit line-item total (`quantity * unit_price`).

---

## 3. Database Constraints & Invariants

FactoryOS implements **defense-in-depth**: invariants are checked at both the Spring validation layer and the PostgreSQL database engine layer:

| Table                  | Constraint                     | Definition                                                   | Enforcement                 |
| :--------------------- | :----------------------------- | :----------------------------------------------------------- | :-------------------------- |
| `products`             | SKU Uniqueness                 | `uk_product_sku (sku)`                                       | Unique B-Tree Index         |
| `products`             | Non-negative price & threshold | `CHECK (unit_price >= 0 AND reorder_level >= 0)`             | PostgreSQL Table Constraint |
| `inventory`            | 1:1 Product Mapping            | `uk_inventory_product_id (product_id)`                       | Unique B-Tree Index         |
| `inventory`            | Non-negative Stock Balances    | `CHECK (quantity_available >= 0 AND reserved_quantity >= 0)` | PostgreSQL Table Constraint |
| `purchase_orders`      | Sequential Order Number        | `uk_purchase_order_number (order_number)`                    | Unique B-Tree Index         |
| `purchase_orders`      | Non-negative Total Amount      | `CHECK (total_amount >= 0)`                                  | PostgreSQL Table Constraint |
| `purchase_order_items` | Positive Quantity & Prices     | `CHECK (quantity > 0 AND unit_price >= 0 AND subtotal >= 0)` | PostgreSQL Table Constraint |
