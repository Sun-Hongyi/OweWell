# OweWell

Split group expenses and settle up in the **fewest payments possible**.

## Status
🚧 Work in progress. The domain logic, REST API and PostgreSQL persistence are complete; sign-in and the frontend are next.

## What works so far
- **Money handling** in integer cents, so no rounding errors and no lost cents when splitting
- **Four split types** behind a `SplitStrategy` interface, all guaranteed to add up exactly to the total:
  - `EqualSplit`: everyone pays the same ($10 ÷ 3 = $3.34 + $3.33 + $3.33)
  - `SharesSplit`: split by shares ("2 drinks vs 1")
  - `PercentageSplit`: split by percentages that must total 100%
  - `ExactSplit`: each person's exact amount
- **Expenses** that reject shares not matching the total
- **Balances** showing who is owed and who owes across many expenses
- **Event-sourced ledger:** expenses, voids and payments are stored as events and never deleted; balances are calculated by replaying the history
- **Settlement algorithms** behind a `SettlementAlgorithm` interface:
  - `GreedySettlement`: fast; the biggest debtor pays the biggest creditor
  - `OptimalSettlement`: finds the true minimum number of payments using bitmask dynamic programming, O(n·2ⁿ)
  - `AutoSettlement`: uses optimal for up to 20 people and falls back to greedy for larger groups
- **REST API** for groups, expenses (all four split types), balances, settlement and payments, with clear 400/404 errors
- **PostgreSQL persistence:** ledger events are stored as JSONB in an append-only table and replayed to rebuild each group; the schema is managed with Flyway migrations

## Architecture
Clean (hexagonal) architecture in a single Spring Boot application:
- `domain`: pure Java business rules, with no framework code
- `application`: use cases (`GroupService`) and the `GroupRepository` interface
- `infrastructure`: `PostgresGroupRepository` for the app, plus `InMemoryGroupRepository` for fast tests
- `api`: REST controllers, request/response DTOs and error handling

Swapping in-memory storage for PostgreSQL required no changes to the domain, service or API layers.

## Why "optimal" matters
Minimising the number of payments is NP-hard. Greedy can use more payments than necessary.
For example, with balances of +$100, +$80, −$80, −$70 and −$30, greedy needs 4 payments, while
the optimal algorithm finds 3 by spotting that {+$80, −$80} and {+$100, −$70, −$30} each settle on their own.

## Benchmarks
2,000 random groups per size, with whole-dollar balances between −$100 and +$100:

| People | Greedy (avg payments) | Optimal (avg payments) | Payments saved | Optimal better in | Optimal time per group |
|---|---|---|---|---|---|
| 4  | 2.97  | 2.97 | 0.0% | 0.0%  | 0.001 ms |
| 6  | 4.90  | 4.87 | 0.5% | 2.3%  | 0.004 ms |
| 8  | 6.77  | 6.60 | 2.5% | 17.0% | 0.007 ms |
| 10 | 8.60  | 8.11 | 5.7% | 47.3% | 0.026 ms |
| 12 | 10.40 | 9.61 | 7.6% | 66.5% | 0.035 ms |

Greedy is near-optimal for small groups, but the gap grows with group size. Optimal stays well under a millisecond.

To reproduce, run `SettlementBenchmark` in `src/test/java/owewell/domain/`.

## Tech
Java 25 · Spring Boot 4 · PostgreSQL · Flyway · Docker · Gradle · JUnit 5 · MockMvc · GitHub Actions CI

## Running locally
Requires Docker and Java 25.

```bash
docker compose up -d      # start PostgreSQL
./gradlew bootRun         # start the API on http://localhost:8080
```

Example:
```bash
curl -X POST localhost:8080/groups \
  -H "Content-Type: application/json" \
  -d '{"name":"Bali Trip","members":["alex","sam","priya"]}'
```

## Running the tests
```bash
./gradlew test
```

## Roadmap
- [x] Core domain: money, splits, expenses, balances
- [x] Four split types behind a `SplitStrategy` interface
- [x] Greedy and optimal settlement algorithms
- [x] Benchmarks comparing greedy and optimal
- [x] Event-sourced ledger for expense history
- [x] Groups and application services
- [x] Spring Boot REST API
- [x] PostgreSQL persistence
- [x] Integration tests with Testcontainers
- [x] Conflict handling for concurrent edits
- [ ] Google sign-in and invite links
- [ ] React frontend
- [ ] Deployment
