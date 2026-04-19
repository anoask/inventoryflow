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
