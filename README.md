# transactions-api

A small REST API for managing Transactions (tracked sale events), similar to how Awin tracks
commissionable sales. A transaction starts as `PENDING` and can be reviewed once — approved or
declined.

Built with Java 17, Spring Boot 3.5, PostgreSQL and Flyway; fully dockerized.

## Tech stack

- Java 17, Spring Boot 3.5 (Web, Data JPA, Validation, Actuator)
- PostgreSQL 16, schema managed by Flyway
- Docker & Docker Compose
- Tests: JUnit 5, MockMvc, Testcontainers

## Running it

Requires Docker and Docker Compose.

```bash
docker compose up --build
```

API ready at `http://localhost:8080` once healthy:

```bash
curl http://localhost:8080/actuator/health
```

Stop:

```bash
docker compose down        # add -v to also wipe the database
```

### Without Docker

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/transactions
export SPRING_DATASOURCE_USERNAME=transactions
export SPRING_DATASOURCE_PASSWORD=transactions
./mvnw spring-boot:run
```

### Tests

```bash
./mvnw test
```

DTO tests are plain unit tests; the DAO tests need Docker (they spin up a real Postgres via
[Testcontainers](https://testcontainers.com)).

## API

| Method  | Path                             | Description                                   |
|---------|-----------------------------------|-----------------------------------------------|
| `POST`  | `/api/transactions`               | Create a transaction (starts as `PENDING`)    |
| `GET`   | `/api/transactions`               | List transactions — paginated, filterable     |
| `GET`   | `/api/transactions/{id}`          | Get a single transaction                      |
| `PATCH` | `/api/transactions/{id}/status`   | Approve or decline a `PENDING` transaction    |

### Create a transaction

- Always starts as `PENDING` — the status can't be set from the request.
- `saleAmount` and `commissionAmount` are required and must both be greater than zero.
- Returns `201` with a `Location` header pointing at the new transaction.

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H 'Content-Type: application/json' \
  -d '{"saleAmount": 199.99, "commissionAmount": 9.99}'
```

Invalid amounts return `400` with field-level errors:

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H 'Content-Type: application/json' \
  -d '{"saleAmount": 0, "commissionAmount": 5}'
```

### List transactions

- `status` (optional) — `PENDING`, `APPROVED` or `DECLINED`; omit to list everything.
- `page` (default `0`), `size` (default `20`, capped at `100`).
- `sort` (optional) — e.g. `sort=saleAmount,desc` (defaults to `createdAt,desc`).

```bash
curl "http://localhost:8080/api/transactions?status=APPROVED&page=0&size=20"
```

### Get a transaction

- Returns `404` if the id doesn't exist.

```bash
curl http://localhost:8080/api/transactions/{id}
```

### Approve or decline a transaction

- `status` in the body must be `APPROVED` or `DECLINED` (case-insensitive).
- Any other value, including `PENDING`, returns `400`.
- A transaction can only be reviewed once — reviewing it again returns `409`.

```bash
curl -X PATCH http://localhost:8080/api/transactions/{id}/status \
  -H 'Content-Type: application/json' \
  -d '{"status": "APPROVED"}'
```

## Postman collection

[`postman_collection.json`](postman_collection.json) covers every endpoint above, including the
error cases (invalid amounts, unknown status filter, double review, not-found, malformed id).

Import it directly, no account needed: in Postman, **Import → Link**, then paste:

```
https://raw.githubusercontent.com/alnezt/transactions-api/main/postman_collection.json
```

## Design notes

- **The review rule lives on the entity**, not the service: `Transaction.review()` throws if the
  transaction isn't `PENDING`, so there's no code path that can bypass it.
- **Concurrent reviews are serialized with a pessimistic write lock** (`findByIdForUpdate` in
  `TransactionRepository`). Without it, two simultaneous approve/decline requests could both read
  `PENDING` and both succeed, violating the "only once" rule.
- **Persistence is behind a `TransactionDao`**, so the service layer depends on that instead of
  directly on Spring Data's `JpaRepository`.
- **Filtering uses JPA Specifications** (`TransactionSpecifications`) rather than a derived query
  method, so adding more filters later (date range, amount range, ...) doesn't require rewriting
  the query — just adding and combining another `Specification`.
- **The `> 0` rule on amounts is enforced twice**: as bean validation on the request DTO (for a
  readable error message) and as a `CHECK` constraint in the migration (as a safety net at the
  database level). Deliberate duplication, not an oversight.

## AI usage

**What I used it for:** Implementing the CRUD endpoints, the Docker/Postgres setup, the DAO
refactor, and the test suite.

**Where it helped:** Catching a real bug during testing — an early version returned a stale
`updatedAt` in the API response, because it relied on `@PreUpdate`, which only fires at flush
time, after the response had already been built.

**What I changed or improved:**

- I came up with the domain logic and the incremental, small-steps approach — connecting Docker,
  Postgres and each endpoint one piece at a time so everything stayed verified along the way.
- The `TransactionDao` layer (between the service and `JpaRepository`) was my explicit
  refactoring request, so the service wouldn't depend directly on Spring Data — a Dependency
  Inversion decision I made.
- I ran the Postman collection against the live API myself and found it was stale — requests
  against an `/echo` endpoint already removed from the code, and no coverage for pagination. I
  had Claude update the collection and verified the fixed requests against the running API.
