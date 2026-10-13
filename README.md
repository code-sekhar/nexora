<div align="center">

# NEXORA
### Smart Business Management Platform

**Inventory • Sales • Billing • Purchases • Finance • Reconciliation**

A modular, multi-tenant business management platform built with **Java, Spring Boot and MySQL**.

**Status:** Active Development · **Backend:** Spring Boot REST API · **Frontend:** Planned React application

</div>

---

## ✨ Overview

**NEXORA** is a business management SaaS designed to bring day-to-day retail operations into one connected workspace. It covers product catalogues, inventory, suppliers, purchases, customer sales, payments, expenses, cash/bank accounts, and reconciliation.

The goal is to help small and growing businesses manage operations with clear records, reliable workflows, and useful reporting. **AI-powered capabilities are part of the product vision; they are not represented here as already implemented.**

> **Project maturity:** This repository is under active development. Completed modules may still require security, data-integrity, integration, and release hardening before production deployment.

## 🧩 Features

| Area | Capabilities | Development state |
|---|---|---|
| Identity & access | Authentication, JWT, users, roles | Core implemented |
| Business management | Business records and business-scoped operations | Core implemented |
| Catalogue | Categories, brands, taxes, products | Core implemented |
| Stock & procurement | Inventory, suppliers, purchases, purchase returns | Core implemented |
| Sales | Customers, invoices, payments, sales returns | Core implemented |
| Ledgers | Customer and supplier ledger flows, supplier payments/statements | Core implemented |
| Expenses | Expense categories, expenses, expense payments | Core implemented |
| Banking | Cash/bank accounts, credits, debits, transfers | Core implemented |
| Reconciliation | Reconciliation headers/items, candidate scoring, manual unmatch | Core implemented; further safeguards pending |
| AI & advanced analytics | Intelligent insights and automation | Product roadmap |

**Reconciliation note:** Auto-match currently *suggests* candidate transactions; it does not automatically persist a match. Candidate scoring uses amount, date, type, reference, and description. Manual unmatch is supported.

## 🏗️ Architecture

```text
┌──────────────────────────────┐
│      React Frontend          │  Planned client
└──────────────┬───────────────┘
               │ HTTPS / REST / JSON
┌──────────────▼───────────────┐
│     Spring Boot Backend      │
│  Controllers → Services      │
│      → Repositories          │
│   Spring Security + JWT      │
└──────────────┬───────────────┘
               │ JPA / Hibernate
┌──────────────▼───────────────┐
│          MySQL               │
└──────────────────────────────┘
```

**Multi-tenancy:** Business-owned records must be accessed within the authenticated user's business scope. A record ID alone must never authorize access to another business's data.

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Backend | Spring Boot 4.x |
| API | REST / JSON |
| Security | Spring Security, JWT |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL |
| Build | Maven (if present in the repository) |
| Frontend direction | React |

## 🚀 Local Development

### Prerequisites

- JDK 17
- MySQL running locally
- Maven or the repository's Maven wrapper
- An IDE such as IntelliJ IDEA, Eclipse / Spring Tools, or VS Code

### 1. Clone the repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd <YOUR_REPOSITORY_DIRECTORY>
```

### 2. Create the database

```sql
CREATE DATABASE IF NOT EXISTS nexora
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 3. Configure the application

Create local configuration using the project's existing `application.properties` or environment-based overrides.

Example **development-only** settings:

```properties
spring.application.name=nexora
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3307/nexora
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
```

**Important:** Port `3307` reflects the current local setup; change it if MySQL uses another port. `ddl-auto=update` is suitable only for the current development workflow. Use versioned migrations and schema validation before production. Never commit database passwords or JWT signing secrets.

### 4. Run the backend

With the Maven wrapper (if available):

```bash
./mvnw spring-boot:run
```

Or with an installed Maven executable:

```bash
mvn spring-boot:run
```

Local API base URL:

```text
http://localhost:8081
```

## 🔐 Authentication & Authorization

NEXORA uses JWT-based authentication and role-aware endpoint protection.

- Public registration and login endpoints are provided.
- Protected requests use `Authorization: Bearer <ACCESS_TOKEN>`.
- Business ownership must be checked in service/repository operations.
- Authorization rules must be reviewed per role before production release.

Example login request (replace with the fields supported by the current DTO):

```http
POST /api/auth/login
Content-Type: application/json
```

## 🔄 Core Business Workflow

```text
Supplier → Purchase → Inventory → Sale / Invoice
                                    ↓
                                 Payment
                                    ↓
                          Customer balance / Ledger

Expense → Expense Payment → Account Transaction
Cash / Bank Activity → Reconciliation → Match / Unmatch
```

These flows represent the platform's intended integrated domain model. Validate each cross-module accounting effect before relying on it in production.

## 🧪 API Testing

Use Postman or another REST client. Supply a valid bearer token for protected endpoints.

### Reconciliation example

```http
GET http://localhost:8081/api/reconciliations/1
Authorization: Bearer <ACCESS_TOKEN>
```

```http
GET http://localhost:8081/api/reconciliation-items/reconciliation/1
Authorization: Bearer <ACCESS_TOKEN>
```

```http
POST http://localhost:8081/api/reconciliations/auto-match
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

```json
{
  "reconciliationId": 1,
  "statementAmount": 10000,
  "statementDate": "2026-09-30",
  "statementReference": "TRF-001",
  "statementDescription": "Cash to HDFC bank transfer",
  "transactionType": "TRANSFER"
}
```

```http
DELETE http://localhost:8081/api/reconciliation-items/1
Authorization: Bearer <ACCESS_TOKEN>
```

**Observed local behavior:** Removing the only reconciliation item returned an empty item list, changed the reconciliation to `IN_PROGRESS`, and made the previously matched account transaction eligible for auto-match suggestions again.

## 🗂️ Code Organization

The backend uses feature-oriented packages under `com.nexora`, including:

```text
com.nexora
├── auth/
├── business/
├── config/
├── expense/
├── reconciliation/
└── ... other domain modules
```

Within a module, the usual structure is:

```text
controller/  → HTTP endpoints
dto/         → API requests and responses
service/     → business rules and transactions
repository/  → persistence queries
entity/      → JPA domain models
```

Actual package names and paths should be checked against the repository before publishing.

## 🗺️ Roadmap

- [x] Core identity and business management
- [x] Product, inventory, purchasing, and sales foundations
- [x] Expense and account transaction foundations
- [x] Reconciliation items, suggestions, and unmatch flow
- [ ] Complete reconciliation invariants and concurrency tests
- [ ] Strengthen role permissions and tenant isolation
- [ ] Replace development schema updates with migrations
- [ ] Move secrets to secure environment configuration
- [ ] Improve audit trails, idempotency, and financial locking
- [ ] Build and integrate the React frontend
- [ ] Add reporting, dashboards, and AI-powered assistance
- [ ] Production monitoring, backup/recovery, and release validation

## 🛡️ Security & Production Readiness

Before public deployment, prioritize:

1. **Tenant isolation:** enforce business ownership on every read and write.
2. **Financial integrity:** transactional updates, row locking, duplicate prevention, and consistent ledger/account balances.
3. **Reconciliation correctness:** finalized states, safe re-matching, and statement-to-transaction consistency.
4. **Secret management:** externalized JWT keys and DB credentials.
5. **Schema control:** migration scripts and `ddl-auto=validate`.
6. **Testing:** authorization, concurrency, integration, and regression suites.
7. **Operations:** structured logs, backups, recovery tests, and observability.

## 🤝 Contributing

The project is currently in active development. If collaboration is opened, contributions should include tests, follow existing domain boundaries, and preserve business-scoped authorization. Do not change financial behavior without verifying downstream ledger and account effects.

## 📄 License

**License not yet specified.** No open-source permission is implied until a license file is added.

---

<div align="center">

**NEXORA** — One connected workspace for smarter business operations.

*Built with care. Designed to grow.*

</div>