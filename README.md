# OweWell

Split group expenses and settle up in the **fewest payments possible**.

## Status
🚧 Work in progress. The core domain logic is complete and tested; the API, database and frontend are next.

## What works so far
- **Money handling** in integer cents, so no rounding errors and no lost cents when splitting
- **Equal splits** that always add up exactly to the total ($10 ÷ 3 = $3.34 + $3.33 + $3.33)
- **Expenses** that reject shares not matching the total
- **Balances** showing who is owed and who owes across many expenses
- **Settlement algorithms:**
  - `GreedySettlement`: fast; the biggest debtor pays the biggest creditor
  - `OptimalSettlement`: finds the true minimum number of payments using bitmask dynamic programming, O(n·2ⁿ)
  - `AutoSettlement`: uses optimal for up to 20 people and falls back to greedy for larger groups

## Why "optimal" matters
Minimising the number of payments is NP-hard. Greedy can use more payments than necessary.
For example, with balances of +$100, +$80, −$80, −$70 and −$30, greedy needs 4 payments, while
the optimal algorithm finds 3 by spotting that {+$80, −$80} and {+$100, −$70, −$30} each settle on their own.

## Tech
Java 25 · Gradle · JUnit 5 · GitHub Actions CI

## Running the tests
```bash
./gradlew test
```

## Roadmap
- [x] Core domain: money, splits, expenses, balances
- [x] Greedy and optimal settlement algorithms
- [ ] Benchmarks comparing greedy and optimal
- [ ] Event-sourced ledger for expense history
- [ ] Spring Boot REST API
- [ ] PostgreSQL persistence
- [ ] Google sign-in and invite links
- [ ] React frontend
- [ ] Deployment