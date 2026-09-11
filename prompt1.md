# FactoryOS — Phase 1: Project Setup & Architecture

We are building **FactoryOS — Industrial Inventory Management System**, a portfolio project for a fresher Java/Spring Boot developer role.

Follow the previously provided FactoryOS Master Prompt as the overall project specification.

For this task, implement **ONLY Phase 1**.

Do not implement Product CRUD, Supplier CRUD, Inventory management, Purchase Orders, authentication, frontend, or other future-phase functionality yet.

---

## Phase 1 Objective

Create a clean, professional Spring Boot project foundation that is ready for the later phases.

The application must:

- Use Java 21
- Use Spring Boot 3.x
- Use Maven
- Use PostgreSQL
- Use Spring Web
- Use Spring Data JPA
- Use Hibernate
- Use Jakarta Bean Validation
- Use JUnit 5
- Follow a layered architecture
- Use environment-based database configuration
- Start successfully
- Connect successfully to PostgreSQL

---

# 1. Create the Spring Boot Project

Set up:

```text
Java 21
Spring Boot 3.x
Maven
```

Include the dependencies required for the current and upcoming project:

- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Validation
- Spring Boot Test

Do not add unnecessary dependencies.

If Lombok is used, explain why it is being used and keep its usage reasonable.

---

# 2. Project Structure

Create a clean package structure similar to:

```text
com.factoryos
│
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── exception
├── validation
├── config
└── FactoryOsApplication
```

Do not create empty classes merely to populate every package.

Create the packages that are appropriate for Phase 1 and explain which packages will be populated in later phases.

---

# 3. Application Configuration

Configure PostgreSQL using environment variables.

Use configuration such as:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Do not hardcode credentials.

Create:

```text
.env.example
```

with placeholder values.

Example:

```text
DB_URL=jdbc:postgresql://localhost:5432/factoryos
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

Do not include actual credentials.

---

# 4. PostgreSQL Database

The application will use a PostgreSQL database named:

```text
factoryos
```

Document the command/setup required to create the database.

Do not create the database itself through application startup.

The application should connect to the existing database.

---

# 5. JPA/Hibernate Configuration

Configure JPA/Hibernate appropriately for development.

Use PostgreSQL as the database.

During development, use a schema strategy that makes the initial development process convenient.

For example:

```text
spring.jpa.hibernate.ddl-auto=update
```

Do not use `create` or `create-drop` because the project will eventually contain persistent data.

Also configure:

- SQL logging only if useful during development
- Hibernate formatting if useful
- PostgreSQL dialect only if actually necessary

Avoid unnecessary configuration.

---

# 6. Application Entry Point

Create the main application class:

```text
FactoryOsApplication
```

It should start the Spring Boot application normally.

Verify that:

```text
./mvnw spring-boot:run
```

or the appropriate Maven command starts the application successfully.

---

# 7. Basic Health Endpoint

Create a very small health/status endpoint:

```text
GET /api/health
```

Example response:

```json
{
  "status": "UP",
  "application": "FactoryOS"
}
```

Keep this endpoint simple.

Do not introduce Actuator yet unless there is a strong reason to do so.

---

# 8. Initial Architecture Documentation

Create a basic architecture description in the README.

Show:

```text
Client
   ↓
REST Controller
   ↓
Service Layer
   ↓
Repository Layer
   ↓
PostgreSQL
```

Explain the responsibility of each layer.

Also document that DTOs will be used between the API and domain/persistence layers in later phases.

---

# 9. README — Initial Version

Create a professional README containing:

## FactoryOS

Short project description.

### Features

At this stage, mention that the system will eventually support:

- Product management
- Supplier management
- Inventory tracking
- Stock movements
- Purchase orders
- Low-stock detection
- Validation
- Exception handling
- Automated tests

Clearly distinguish planned features from currently implemented features.

### Tech Stack

```text
Java 21
Spring Boot
Spring Data JPA
Hibernate
PostgreSQL
Maven
JUnit 5
```

### Project Structure

Show the package structure.

### Setup

Explain:

1. Install Java 21.
2. Install PostgreSQL.
3. Create the `factoryos` database.
4. Configure environment variables.
5. Run the application.

### Running

Provide the Maven command.

### Health Check

Document:

```text
GET /api/health
```

and its expected response.

---

# 10. Testing Foundation

Create at least one basic test confirming that the Spring Boot application context loads successfully.

For example, a context-load test.

Do not create meaningless tests just to increase coverage.

The goal is to establish the testing foundation for later phases.

---

# 11. Git-Friendly Project

Create an appropriate `.gitignore`.

It should exclude things such as:

```text
target/
.idea/
*.iml
.env
```

Do not ignore `.env.example`.

Do not commit database credentials.

---

# 12. Code Quality Requirements

Follow these principles:

- Constructor injection
- Clear naming
- Small classes
- No unnecessary abstractions
- No unnecessary design patterns
- No hardcoded secrets
- No unused dependencies
- No dead code
- No excessive comments

Keep the project simple enough for a fresher to understand completely.

---

# 13. Verification

Before completing Phase 1, verify:

### Build

```text
mvn clean test
```

passes successfully.

### Application

The Spring Boot application starts successfully.

### Database

The application successfully connects to PostgreSQL.

### Endpoint

```text
GET /api/health
```

returns HTTP 200.

### Test

The Spring application context test passes.

---

# 14. Final Response Requirements

After implementation, provide:

### 1. What was built

A concise summary.

### 2. Project structure

Show the important directories/files.

### 3. Dependencies

Explain why each major dependency exists.

### 4. Configuration

Explain how PostgreSQL configuration works.

### 5. Architecture

Explain the current architecture and how it will evolve.

### 6. How to run

Give the exact commands.

### 7. Verification

Show what was tested and the result.

### 8. Interview Notes

Explain the important Phase 1 concepts I should understand for an interview, especially:

- Why Spring Boot?
- Why Maven?
- Why PostgreSQL?
- Why JPA/Hibernate?
- Why layered architecture?
- Why environment variables?
- Why constructor injection?
- What is dependency injection?
- What is the role of `@SpringBootApplication`?

---

## Important

Implement **ONLY Phase 1**.

Do not start Phase 2 automatically.

Do not add Product, Supplier, Inventory, Purchase Order, authentication, JWT, frontend, Docker, Redis, Kafka, or microservices yet.

At the end, clearly state:

**Phase 1 complete — waiting for Phase 2.**

with every phase store the phase-walkthroughs in side '.docs\walkthroughts' along with a md file called 'installation-and-guide'.
