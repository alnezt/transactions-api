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

Requires Docker and Docker Compose. From the project root:

```bash
docker compose up --build
This starts Postgres and the API together. On first boot, Flyway creates the transactions table
and seeds 3 sample rows (one per status). The API is ready when it passes its healthcheck:


curl http://localhost:8080/actuator/health
# {"status":"UP", ...}
The API listens on http://localhost:8080. Postgres is exposed on host port 5433 (mapped from
its usual 5432, to avoid clashing with a local Postgres install) — you generally won't need it
directly, but jdbc:postgresql://localhost:5433/transactions (user/password: transactions) works
if you want to inspect it.

To stop everything: docker compose down (add -v to also drop the database volume and reset the
seed data).

Running locally without Docker

./mvnw spring-boot:run
Needs a Postgres instance reachable at localhost:5432 (or override via env vars, see
application.yml):


export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/transactions
export SPRING_DATASOURCE_USERNAME=transactions
export SPRING_DATASOURCE_PASSWORD=transactions
Running the tests

./mvnw test
The DAO tests spin up a real Postgres via Testcontainers, so Docker
needs to be running.

API
Method	Path	Description
POST	/api/transactions	Create a transaction (starts as PENDING)
GET	/api/transactions	List transactions — paginated, filterable
GET	/api/transactions/{id}	Get a single transaction
PATCH	/api/transactions/{id}/status	Approve or decline a PENDING transaction
Create a transaction
POST /api/transactions — records a new sale event, always starting in PENDING status.
saleAmount and commissionAmount are required and must both be greater than zero.


curl -X POST http://localhost:8080/api/transactions \
  -H 'Content-Type: application/json' \
  -d '{"saleAmount": 199.99, "commissionAmount": 9.99}'

{
  "id": "a78225c7-c707-43e6-bbe1-343acfbea0c4",
  "status": "PENDING",
  "saleAmount": 199.99,
  "commissionAmount": 9.99,
  "createdAt": "2026-08-18T08:08:20.94Z",
  "updatedAt": "2026-08-18T08:08:20.94Z"
}
Invalid amounts return 400 Bad Request with field-level errors:


curl -X POST http://localhost:8080/api/transactions \
  -H 'Content-Type: application/json' \
  -d '{"saleAmount": 0, "commissionAmount": 5}'
# 400 {"errors":{"saleAmount":"must be greater than 0"}}
List transactions
GET /api/transactions — returns transactions page by page, newest first by default.

status (optional) — filter by PENDING, APPROVED or DECLINED; omit to list everything.
page (default 0), size (default 20, capped at 100).
sort (optional) — e.g. sort=saleAmount,desc (defaults to createdAt,desc).

curl "http://localhost:8080/api/transactions?status=APPROVED&page=0&size=20"

{
  "content": [ { "id": "...", "status": "APPROVED", "...": "..." } ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1
}
Get a transaction
GET /api/transactions/{id} — fetches one transaction by id. Returns 404 if it doesn't exist.


curl http://localhost:8080/api/transactions/a78225c7-c707-43e6-bbe1-343acfbea0c4
Approve / decline a transaction
PATCH /api/transactions/{id}/status — the only way to move a transaction out of PENDING.
status in the body must be APPROVED or DECLINED (case-insensitive) — anything else, including
PENDING, returns 400 Bad Request. A transaction can only be reviewed once: reviewing it again
returns 409 Conflict.


curl -X PATCH http://localhost:8080/api/transactions/a78225c7-c707-43e6-bbe1-343acfbea0c4/status \
  -H 'Content-Type: application/json' \
  -d '{"status": "APPROVED"}'
Postman collection
postman_collection.json covers every endpoint above, including the
error cases (invalid amounts, unknown status filter, double review, not-found, malformed id).

Import it directly, no account needed: in Postman, Import → Link, then paste:


https://raw.githubusercontent.com/alnezt/transactions-api/main/postman_collection.json
Design notes
The review rule lives on the entity, not the service: Transaction.review() throws if the transaction isn't PENDING, so there's no code path that can bypass it.
Concurrent reviews are serialized with a pessimistic write lock (findByIdForUpdate in TransactionRepository). Without it, two simultaneous approve/decline requests could both read PENDING and both succeed, violating the "only once" rule.
Persistence is behind a TransactionDao, so the service layer depends on that instead of directly on Spring Data's JpaRepository.
Filtering uses JPA Specifications (TransactionSpecifications) rather than a derived query method, so adding more filters later (date range, amount range, ...) doesn't require rewriting the query — just adding and combining another Specification.
The > 0 rule on amounts is enforced twice: as bean validation on the request DTO (for a readable error message) and as a CHECK constraint in the migration (as a safety net at the database level). Deliberate duplication, not an oversight.
AI usage
What I used it for: Implementing the CRUD endpoints, the Docker/Postgres setup, the DAO refactor, and the test suite, adding comments, dependencies.
Where it helped: Catching a real bug during testing - an early version returned a stale updatedAt in the API response, because it relied on @PreUpdate, which only fires at flush time, after the response had already been built.
What I changed or improved: I came up with the logic giving it very clear instructions, adding Docker/Postgres, going for an implementation where I connected everything in small steps in order to have everything checked.
The TransactionDao layer (between the service and JpaRepository) was my explicit refactoring request, so the service wouldn't depend directly on Spring Data - a Dependency Inversion decision I made.
Verified generated output against an external artifact and found inconsistencies.
The Postman collection had requests against an /echo endpoint already removed from the code, and no pagination examples - caught by checking it against the live API, not just reading the code.
