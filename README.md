# Finance Dashboard Backend

Production-style Spring Boot API for a finance dashboard: REST for auth, users, and transactions; **GraphQL** for dashboard analytics; **JWT** authentication; **RBAC** enforced on the service layer; **PostgreSQL** with **Flyway**; **Bucket4j** rate limiting (in-memory, swappable for Redis); **audit logging** for mutations.

## Stack

- Java 17, Spring Boot 3.2, Maven  
- PostgreSQL, Spring Data JPA, Flyway  
- Spring Security + JWT (jjwt)  
- Spring for GraphQL  
- Bucket4j (in-memory `ConcurrentHashMap` buckets; keys are `user:{id}` when authenticated, else `ip:{address}`)  
- MapStruct (entity ↔ DTO)  
- Tests: JUnit 5, Mockito; integration tests with **Testcontainers** (PostgreSQL) — requires Docker when running `FinanceApplicationIT`

## RBAC

| Role     | Users API | `GET /api/transactions` (all) | `GET /api/transactions/mine` | Create / update / delete own tx | Dashboard GraphQL |
|----------|-----------|-------------------------------|------------------------------|--------------------------------|-------------------|
| **ADMIN**  | Full      | Yes                           | Yes                          | Yes (any via ownership rules)  | Global aggregates |
| **ANALYST**| No        | Yes                           | Yes                          | Own transactions only          | Global aggregates |
| **VIEWER** | No        | No (403)                      | Yes                          | Own transactions only          | **Scoped to own** user id |

- **Register** always creates **VIEWER** (role escalation only via `PATCH /api/users/{id}/role` by an admin).  
- **Update/delete transaction**: **ADMIN** or **owner** (`@authz.canModifyTransaction` + `@PreAuthorize` on `TransactionService`).

## API (REST)

| Method | Path | Notes |
|--------|------|--------|
| POST | `/api/auth/register` | Body: `email`, `password` |
| POST | `/api/auth/login` | Returns JWT |
| GET | `/api/users` | Admin |
| PATCH | `/api/users/{id}/role` | Admin |
| PATCH | `/api/users/{id}/status` | Admin |
| GET | `/api/transactions` | Admin, Analyst — query: `type`, `category`, `from`, `to`, `page`, `size` |
| GET | `/api/transactions/mine` | Authenticated |
| POST | `/api/transactions` | Creates row for **current user** |
| PUT | `/api/transactions/{id}` | Admin or owner |
| DELETE | `/api/transactions/{id}` | Soft delete; admin or owner |

Header: `Authorization: Bearer <jwt>`

## GraphQL

- Endpoint: `POST /graphql` (authenticated).  
- GraphiQL (dev): `http://localhost:8080/graphiql` — configure HTTP header `Authorization: Bearer <token>` in the UI.

Example:

```graphql
query {
  dashboardSummary(dateFrom: "2024-01-01", dateTo: "2025-12-31") {
    totalIncome
    totalExpense
    netBalance
  }
  categoryBreakdown(dateFrom: "2024-01-01", dateTo: "2025-12-31") {
    category
    totalAmount
    type
  }
  monthlyTrends(dateFrom: "2024-01-01", dateTo: "2025-12-31") {
    month
    income
    expense
  }
  recentTransactions(limit: 5, dateFrom: "2024-01-01", dateTo: "2025-12-31") {
    id
    amount
    type
    category
    date
  }
}
```

Aggregations run in the database (native SQL / JPQL), not in-memory over full tables.

## Configuration and secrets

Secrets and environment-specific values belong in a **local `.env` file** (gitignored), not in Git.

1. Copy the template and edit values:

   ```bash
   copy .env.example .env
   ```

   (PowerShell: `Copy-Item .env.example .env`)

2. On startup, **`DotenvBootstrap`** loads `.env` into JVM system properties **before** Spring starts. **Existing OS environment variables are never overwritten** by the file, so Docker/Kubernetes/CI secrets keep precedence.

3. `src/main/resources/application.yml` uses placeholders with local defaults for the datasource. **Do not** put placeholder strings like `YOUR_DB_URL` or `${SPRING_DATASOURCE_URL}` in `.env` for `SPRING_DATASOURCE_URL` — use a real `jdbc:postgresql://...` URL or omit that variable so the YAML default applies.

| Variable | Purpose |
|----------|---------|
| `SPRING_DATASOURCE_URL` | JDBC URL (default in yml: `localhost:5432/finance`) |
| `SPRING_DATASOURCE_USERNAME` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | DB password (**required** for a real database) |
| `APP_JWT_SECRET` | JWT HS256 key (**required**, ≥ 32 characters) |
| `APP_JWT_EXPIRATION_MS` | Token lifetime (default 86400000) |
| `SERVER_PORT` | HTTP port (default 8080) |
| `APP_RATE_LIMIT_REQUESTS_PER_MINUTE` | Bucket4j limit (default 100) |
| `SPRING_GRAPHQL_GRAPHIQL_ENABLED` | `true`/`false` — set **`false` in production** |

**Startup checks:** `JwtSecretValidator` runs for every profile except `test` and fails fast if `APP_JWT_SECRET` is missing, empty, or shorter than 32 characters.

Dependency: **dotenv-java** (see `pom.xml`).

## Database

Flyway: `src/main/resources/db/migration/V1__init.sql`

**Note:** The assignment mentioned a column named `timestamp` on `audit_log`. This project uses **`logged_at`** instead to avoid PostgreSQL identifier ambiguity; semantics are unchanged.

## Run locally

1. Create `.env` from `.env.example` and set **`APP_JWT_SECRET`** (≥ 32 characters). For local Postgres matching the YAML defaults (`localhost:5432/finance`, `postgres` / `admin`), you can omit datasource variables in `.env`; uncomment or set them only when you use a non-default database.

2. Start PostgreSQL and create DB/user (match your `.env`):

   ```sql
   CREATE DATABASE finance;
   CREATE USER finance WITH PASSWORD 'your-password';
   GRANT ALL PRIVILEGES ON DATABASE finance TO finance;
   ```

   For schema creation, grant usage on schema `public` to `finance` if needed.

3. Build and run from the project root (so `.env` is found):

   ```bash
   mvn spring-boot:run
   ```

4. **Swagger UI** (OpenAPI 3): [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) — call **POST /api/auth/login**, copy `token`, click **Authorize**, paste the token (Swagger adds `Bearer`). Raw spec: `/v3/api-docs`. GraphQL is not included; use `/graphiql` or **POST /graphql** with the same JWT.

5. Demo users (seeded only when the `users` table is empty):

   | Email | Password | Role |
   |-------|----------|------|
   | admin@finance.local | ChangeMe123! | ADMIN |
   | analyst@finance.local | ChangeMe123! | ANALYST |
   | viewer@finance.local | ChangeMe123! | VIEWER |

## Rate limiting

Returns **429** with a small JSON body when the per-key bucket is exhausted. The filter runs **after** JWT parsing so authenticated traffic is keyed by **user id**.

## Tests

```bash
mvn test
```

- Unit tests: Mockito (no Docker).  
- `FinanceApplicationIT`: Testcontainers PostgreSQL — skipped automatically if Docker is unavailable (`@Testcontainers(disabledWithoutDocker = true)`).

### Manual end-to-end (Postman-style) script

With the app running on `http://localhost:8080` and seeded or valid users:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/e2e-api-test.ps1
```

The script exercises login (admin / analyst / viewer), user list and RBAC, transaction list/mine/CRUD, GraphQL `dashboardSummary`, register, and unauthenticated GraphQL (expects 401 or 403).

## Error format (REST)

`@RestControllerAdvice` returns JSON such as:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "...",
  "timestamp": "2026-04-04T12:00:00Z",
  "fieldErrors": { "email": "must be a well-formed email address" }
}
```

GraphQL errors are mapped via `GraphqlExceptionResolver` where applicable.
