# InventoryFlow

---

## Features

- **Auth:** Register (staff), login, JWT bearer access, role-based routes (`ADMIN`, `STAFF`).
- **Catalog:** Products and suppliers with **search**, **sort**, and **pagination** (paged JSON from the API).
- **Orders:** Purchase orders (admin: create, receive → increases stock) and sales orders (create → decreases stock; **409** when stock is insufficient).
- **Dashboard:** Low-stock list, recent purchase/sales order snippets (where permitted by role).
- **API errors:** JSON bodies with a stable `code` (e.g. `INSUFFICIENT_STOCK`, `VALIDATION_FAILED`) plus `message` / `details` where applicable.

---

## Repository layout

This repo is a **small monorepo**: the Spring Boot app lives at the **repository root** (Maven `pom.xml` + `src/main/java`), and the SPA lives under **`frontend/`**.

```text
InventoryFlow/
├── Dockerfile                   # Optional: container build (Railway, etc.)
├── pom.xml                      # Backend Maven project
├── src/main/java/               # Spring Boot application
├── src/main/resources/
│   ├── application.properties       # Local / default config
│   └── application-prod.properties  # Production profile (SPRING_PROFILES_ACTIVE=prod)
├── backend.env.example          # Backend env template (copy to backend.env)
├── frontend/                    # Vite + React SPA
│   ├── .env.example             # Frontend env template (copy to .env)
│   ├── vercel.json              # SPA fallback for Vercel
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

<<<<<<< HEAD
## Screenshots
=======
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
| `PORT` | HTTP port (Railway injects this; default `8080` locally). |
| `SPRING_PROFILES_ACTIVE` | Set to `prod` for production-style config (JWT required, seed off by default, SSL defaults). |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS` | MySQL connection (local or generic). |
| `DB_USE_SSL` | `false` locally (default); use `true` for managed MySQL (e.g. Railway). |
| `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD` | Optional Railway MySQL plugin variables (override `DB_*` when present). |
| `INVENTORYFLOW_JWT_SECRET` | JWT signing secret (**≥ 32 characters**). **Required** when `SPRING_PROFILES_ACTIVE=prod` (no default). |
| `INVENTORYFLOW_JWT_EXPIRATION_MS` | Optional token lifetime (ms). |
| `INVENTORYFLOW_JWT_ISSUER` | Optional JWT issuer claim. |
| `INVENTORYFLOW_CORS_ORIGINS` | Comma-separated allowed browser origins (e.g. `https://your-app.vercel.app`). Needed for the SPA on Vercel to call the API. |
| `INVENTORYFLOW_SEED_DATA` | `true` to run demo seed on startup (empty DB only). **Prod profile defaults to `false`.** Set `true` once for a resume demo, then `false`. |

### Frontend

From `frontend/`:

```bash
cp .env.example .env
```

| Variable | Purpose |
|----------|---------|
| `VITE_API_BASE_URL` | Base URL of the Spring API (**no trailing slash**). Local: `http://localhost:8080`. Production: your public Railway URL, e.g. `https://inventoryflow-production.up.railway.app`. **Set this in Vercel → Project → Environment Variables** for Production (and Preview if you use preview APIs). |

---

## Setup: database

Create a database (example name matches defaults):

```sql
CREATE DATABASE inventoryflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Ensure `DB_USER` / `DB_PASS` can connect. Hibernate `ddl-auto` is `update` by default (good for local and portfolio demos). With **`SPRING_PROFILES_ACTIVE=prod`**, schema behavior is still `update` for a simple first deploy; for a real production system, prefer **Flyway/Liquibase** and switch to **`validate`**.

---

## Deployment (recommended: Railway + Vercel)

**Easiest portfolio combo:** **Railway MySQL** → **Railway Spring Boot** (Dockerfile at repo root) → **Vercel** static frontend from `frontend/`. All three have free tiers suitable for demos; you wire them together with environment variables only—no Kubernetes.

| Piece | Role |
|--------|------|
| **Railway MySQL** | Managed database; plugin exposes `MYSQL*` variables to your backend service. |
| **Railway (Java API)** | Builds/runs the `Dockerfile`; set `SPRING_PROFILES_ACTIVE=prod`, JWT secret, CORS, and optional seeding. |
| **Vercel** | Hosts the Vite production build; set `VITE_API_BASE_URL` to your Railway public API URL. |

**Repo changes already in place:** production profile (`application-prod.properties`), **CORS** from `INVENTORYFLOW_CORS_ORIGINS`, **optional demo seeding** via `INVENTORYFLOW_SEED_DATA`, root **`Dockerfile`**, **`frontend/vercel.json`** for client-side routes, and **`PORT`** support.

### Deployment order

1. **Database** — Create a MySQL database on Railway (or attach the MySQL plugin). Note the host, port, database name, user, and password (or rely on `MYSQL*` variables injected into the backend service).
2. **Backend** — Create a Railway service from this GitHub repo. Use **Dockerfile** deploy (root directory = repo root). Set environment variables (see below). Deploy and copy the **public HTTPS URL** of the API.
3. **Frontend** — Create a Vercel project with **root directory** `frontend`. Set `VITE_API_BASE_URL` to the Railway API URL (no trailing slash). Deploy.

### Backend (Railway)

1. New project → **Deploy from GitHub** → select this repo.  
2. Add **MySQL** (or use Railway’s MySQL template) and **link** it to the Java service so `MYSQLHOST`, `MYSQLUSER`, etc. are available (or set `DB_*` manually).  
3. In the **Java** service, set variables (minimum for first boot):

| Name | Example / notes |
|------|------------------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `INVENTORYFLOW_JWT_SECRET` | Long random string (≥ 32 characters). |
| `INVENTORYFLOW_CORS_ORIGINS` | `https://<your-vercel-app>.vercel.app` (comma-separated if several). |
| `INVENTORYFLOW_SEED_DATA` | `true` **once** on an empty database for demo users/products; then set to `false` for cleaner “production” behavior. |
| `PORT` | Usually **automatic** on Railway (do not hardcode in code). |

4. **Smoke test:** Check deploy logs for `Started InventoryFlowApplication`, then sign in from your Vercel app or send `POST /api/auth/login` with the demo credentials (after seeding is enabled once).  
5. **Build:** Railway runs `docker build` using the root `Dockerfile` (multi-stage Maven + JRE).

### Frontend (Vercel)

1. New project → import repo → set **Root Directory** to `frontend`.  
2. **Environment variables** (Production): `VITE_API_BASE_URL` = `https://<your-railway-service>.up.railway.app` (your real URL, no trailing slash).  
3. **Build:** `npm run build` (default for Vite). **Output:** `dist`.  
4. **Routing:** `frontend/vercel.json` rewrites unknown paths to `index.html` so React Router works on refresh.

After changing `VITE_API_BASE_URL`, trigger a **redeploy** so Vite bakes the new value into the build.

### Railway backend keeps crashing — check these first

1. **Deploy logs:** `inventoryflow` service → **Deployments** → latest → **View logs**. Scroll to the first **`Caused by:`** line.
2. **Database:** The Java service must have **`MYSQLHOST`**, **`MYSQLPORT`**, **`MYSQLDATABASE`**, **`MYSQLUSER`**, **`MYSQLPASSWORD`** (use **Variable references** from the MySQL service). Do **not** rely on `MYSQL_PUBLIC_URL` for Spring. If logs show **`Connection refused`** talking to MySQL, the app is still using **`localhost`** inside the container—those **`MYSQL*`** variables are not on this service (add references and redeploy).
3. **`PORT`:** Delete a manual `PORT` variable if it is **empty**; let Railway inject `PORT` automatically.
4. **JWT:** `INVENTORYFLOW_JWT_SECRET` must be set and **≥ 32 characters** in `prod`. Too short or missing → startup failure (logs will now say this explicitly after you redeploy the latest code).
5. **Memory:** Free/small plans can OOM during heavy traffic; the `Dockerfile` sets container-friendly JVM memory flags to reduce random exits.

### Database / schema (production-style)

- **First deploy (portfolio):** With `ddl-auto=update`, Hibernate creates/updates tables from JPA entities—simple and common for demos.  
- **Seeding:** Demo users (`admin@inventoryflow.test` / `admin123`, etc.) are created **only** when `inventoryflow.seed.enabled` is true (local default **true**; **`prod`** profile defaults to **`false`** unless `INVENTORYFLOW_SEED_DATA=true`). Seeding is **idempotent** (only fills empty tables).  
- **Stricter production:** Add migrations (Flyway) and set `spring.jpa.hibernate.ddl-auto=validate`—not required for a student portfolio path.

### Local production smoke test

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=localhost
export DB_PASS=...
export INVENTORYFLOW_JWT_SECRET=your-long-random-secret-at-least-32-chars
export INVENTORYFLOW_CORS_ORIGINS=http://localhost:5173
export INVENTORYFLOW_SEED_DATA=true
mvn -DskipTests spring-boot:run
```

---

## Setup: backend

From the **repository root**:

```bash
export DB_PASS=your_mysql_password   # omit or leave empty if your user has no password
export INVENTORYFLOW_JWT_SECRET=your-long-random-secret-at-least-32-chars
mvn spring-boot:run
```

API default port: **8080**.

When **`inventoryflow.seed.enabled`** is true (default locally; controlled by `INVENTORYFLOW_SEED_DATA` in **`prod`**), a **data seeder** creates sample roles, users, suppliers, and products (see demo credentials below).

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

- Docker Compose for MySQL + API + one-command demo
- Server-side pagination for order lists
- Flyway migrations + `ddl-auto=validate` for stricter production
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
>>>>>>> 14ecfba (deploy pass)
