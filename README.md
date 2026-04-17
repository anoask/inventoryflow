# InventoryFlow

**InventoryFlow** is a full-stack inventory and order-management demo: a **Spring Boot** REST API with **JWT auth**, **MySQL**, and a **React + TypeScript + Vite** dashboard. It is structured as a portfolio project—clear domain boundaries, consistent API errors, pagination/search on catalog endpoints, and a polished UI (loading states, toasts, confirmations, role-aware views).

**Resume snapshot:** Designed and shipped a JWT-secured REST backend with JPA and business rules (stock updates from purchase/sales orders), paired with a React SPA that consumes typed APIs, handles auth and role-based navigation, and presents recruiter-friendly UX for catalog and order flows.

---

## Features

- **Auth:** Register (staff), login, JWT bearer access, role-based routes (`ADMIN`, `STAFF`).
- **Catalog:** Products and suppliers with **search**, **sort**, and **pagination** (paged JSON from the API).
- **Orders:** Purchase orders (admin: create, receive → increases stock) and sales orders (create → decreases stock; **409** when stock is insufficient).
- **Dashboard:** Low-stock list, recent purchase/sales order snippets (where permitted by role).
- **API errors:** JSON bodies with a stable `code` (e.g. `INSUFFICIENT_STOCK`, `VALIDATION_FAILED`) plus `message` / `details` where applicable.

---

## Tech stack

| Layer | Technologies |
|--------|----------------|
| Backend | Java 21, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, MySQL, Jakarta Validation, JJWT |
| Frontend | React 18, TypeScript, Vite, React Router, Axios |

---

## Repository layout

This repo is a **small monorepo**: the Spring Boot app lives at the **repository root** (Maven `pom.xml` + `src/main/java`), and the SPA lives under **`frontend/`**.

```text
InventoryFlow/
├── pom.xml                      # Backend Maven project
├── src/main/java/               # Spring Boot application
├── src/main/resources/          # application.properties
├── backend.env.example          # Backend env template (copy to backend.env)
├── frontend/                    # Vite + React SPA
│   ├── .env.example             # Frontend env template (copy to .env)
│   └── src/
├── docs/screenshots/            # Add portfolio screenshots here
├── LICENSE
└── README.md
```

---

## Architecture (high level)

```text
Browser (React SPA)
    │  HTTPS / JSON
    ▼
Spring Boot REST (`/api/...`)
    ├── Security: JWT filter + role checks
    ├── Services: products, suppliers, orders, dashboard
    └── JPA → MySQL
```

The frontend calls the API using a configurable base URL (`VITE_API_BASE_URL`). The backend issues JWTs on login and enforces authorization on controllers.

---

## Prerequisites

- **Java 21** and **Maven**
- **Node.js 20+** and npm
- **MySQL 8+** with a database you can point the app at (e.g. `inventoryflow`)

---

## Environment variables

### Backend

Copy `backend.env.example` to `backend.env` (optional, gitignored), set values, then export them before starting the app—or configure the same keys in your IDE.

| Variable | Purpose |
|----------|---------|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | MySQL connection |
| `DB_USER`, `DB_PASS` | MySQL credentials (`DB_PASS` may be empty for local users with no password) |
| `INVENTORYFLOW_JWT_SECRET` | JWT signing secret (**≥ 32 characters** for HS256). Override in any shared or production environment. |
| `INVENTORYFLOW_JWT_EXPIRATION_MS` | Optional token lifetime (ms) |
| `INVENTORYFLOW_JWT_ISSUER` | Optional JWT issuer claim |

### Frontend

From `frontend/`:

```bash
cp .env.example .env
```

| Variable | Purpose |
|----------|---------|
| `VITE_API_BASE_URL` | Base URL of the Spring API (no trailing slash), e.g. `http://localhost:8080` |

---

## Setup: database

Create a database (example name matches defaults):

```sql
CREATE DATABASE inventoryflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Ensure `DB_USER` / `DB_PASS` can connect. Hibernate `ddl-auto` is set to `update` for development (see `application.properties`).

---

## Setup: backend

From the **repository root**:

```bash
export DB_PASS=your_mysql_password   # omit or leave empty if your user has no password
export INVENTORYFLOW_JWT_SECRET=your-long-random-secret-at-least-32-chars
mvn spring-boot:run
```

API default port: **8080**.

On startup, a **data seeder** creates sample roles, users, suppliers, and products (see demo credentials below).

---

## Setup: frontend

```bash
cd frontend
npm install
cp .env.example .env   # adjust VITE_API_BASE_URL if needed
npm run dev
```

Dev server default: **http://localhost:5173/**

Production build:

```bash
npm run build
```

---

## Demo credentials (seed data)

These accounts are created by the backend seeder for **local/demo** use only—**do not** rely on them in production.

| Role | Email | Password |
|------|--------|----------|
| ADMIN | `admin@inventoryflow.test` | `admin123` |
| STAFF | `staff@inventoryflow.test` | `staff123` |

---

## API summary

Base path: **`/api`**

- **Auth:** `POST /api/auth/register`, `POST /api/auth/login`
- **Dashboard:** `GET /api/dashboard/low-stock`; recent PO/SO endpoints (role-gated)
- **Products / suppliers:** CRUD + **paged** `GET` with `search`, `page`, `size`, `sort`
- **Purchase orders (admin):** create, list, receive (`PATCH .../receive` increases stock)
- **Sales orders:** create (staff/admin); listing may be admin-only depending on endpoint

See source controllers under `com.inventoryflow.controller` for the full set of routes and role rules.

---

## Screenshots

Add images under **`docs/screenshots/`** (for example: login, dashboard, products table, sales order flow) and reference them from your portfolio or GitHub README once uploaded.

---

## Future improvements

- Docker Compose for MySQL + API + optional SPA build
- Server-side pagination for order lists
- Audit log or export (CSV) for inventory changes

---

## Publishing to GitHub

From the repository root (after reviewing `git status`—no `node_modules/`, `target/`, `.env`, or secrets):

```bash
git init
git add .
git commit -m "Initial commit: InventoryFlow full-stack portfolio"
git branch -M main
git remote add origin https://github.com/<YOUR_USERNAME>/<REPO_NAME>.git
git push -u origin main
```

Create an empty repository on GitHub first, then use its URL in `git remote add`.

---

## License

This project is licensed under the **MIT License** — see [`LICENSE`](LICENSE).

---

## GitHub metadata (suggestions)

- **Repository name:** `inventoryflow` (or `inventoryflow-fullstack`)
- **Short description:** *Full-stack inventory & orders demo — Spring Boot, JWT, MySQL, React + TypeScript + Vite*
- **Topics:** `spring-boot`, `java`, `mysql`, `jwt`, `react`, `typescript`, `vite`, `fullstack`
