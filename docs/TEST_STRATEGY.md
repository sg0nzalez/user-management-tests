# Test Strategy

What is tested, in which environments, and how quality is decided. For project structure, see [ARCHITECTURE.md](ARCHITECTURE.md). For run commands, see [README.md](../README.md).

## Purpose

End-to-end validation of the User Management API against the OpenAPI contract in [`sdet_challenge_api.yml`](sdet_challenge_api.yml). Tests exercise real HTTP calls against the Docker-hosted API and assert spec-correct status codes, response bodies, and error shapes.

## Scope

Five REST operations, each run against either `/dev` or `/prod` via `-Denv`:

| Operation | Endpoint | Auth |
|-----------|----------|------|
| List users | GET `/{env}/users` | No |
| Create user | POST `/{env}/users` | No |
| Get user | GET `/{env}/users/{email}` | No |
| Update user | PUT `/{env}/users/{email}` | No |
| Delete user | DELETE `/{env}/users/{email}` | `Authentication` header |

## Out of scope

- UI or browser automation
- Load or stress testing beyond the optional local TTFB suite
- Application source code coverage metrics
- Third-party services other than the challenge Docker image

## Coverage matrix

| Endpoint | Test class | Scenarios |
|----------|------------|-----------|
| GET /users | `ListUsersTests` | 200 + list schema; includes created user; excludes deleted user; ignores extra query params |
| POST /users | `CreateUserTests` | 201 happy path; age min/max; missing/empty/malformed fields; invalid/injection-like email; extra fields ignored; wrong Content-Type; script-like name; duplicate email; POST on resource path → 405 |
| GET /users/{email} | `GetUserTests` | 200 existing user; 404 unknown; injection/path-traversal paths; ignores extra query params |
| PUT /users/{email} | `UpdateUserTests` | 200 update; age min/max; 404/400/409 paths; extra fields ignored; update visible on subsequent GET (BUG-007); PUT/PATCH on wrong paths → 405 |
| DELETE /users/{email} | `DeleteUserTests` | 204 + gone from GET/list; 401 missing/invalid/empty/wrong-header token; 404 unknown/injection path; DELETE on collection → 405 |
| Multi-step flow | `UserLifecycleTests` | Create → get → update → get → delete → get/list |
| All endpoints (TTFB) | `UsersTtfbTests` | Parallel probes per endpoint; p90 under threshold |

## Environments

| Env | Path prefix | Database | Config key |
|-----|-------------|----------|------------|
| DEV | `/dev` | Isolated | `DEV_API_BASE_URL` |
| PROD | `/prod` | Isolated | `PROD_API_BASE_URL` |

Both environments share the same Docker container on port 3000. Auth tokens for DELETE are stored encrypted in `auth.properties` and decrypted at runtime using `ENCRYPTION_MASTER_KEY`.

## CI policy

| Trigger | What runs | TTFB? |
|---------|-----------|-------|
| `pull_request` | Quality gates + parallel DEV and PROD functional tests | Never |
| `workflow_dispatch` | Quality gates + single chosen env and suite | Only if `ttfb.xml` selected |
| `push` | Nothing | — |

Dependabot PRs follow the same `pull_request` path (both envs, functional suite only).

TTFB is intentionally excluded from the merge pipeline because runner latency can cause false p90 failures.

## Defect handling

The OpenAPI spec is the source of truth. When the API behaves differently from the spec, tests assert the spec-correct behavior and a bug ticket is filed under [`docs/bugs/`](bugs/) using [`BUG-TEMPLATE.md`](bugs/BUG-TEMPLATE.md). Tests remain spec-strict; they are not adjusted to match incorrect API behavior without a documented defect.

## TTFB suite (intent)

Optional performance checks using JDK `HttpClient` parallel probes:

| Setting | Value |
|---------|--------|
| Concurrency | 20 parallel requests per endpoint |
| Assertion | p90 &lt; 250 ms (nearest-rank: 18th of 20 sorted samples) |
| Transport failures | Must be 0 |
| Measurement | Time until status and headers arrive |

Runs locally or via GitHub Actions **Run workflow** with `ttfb.xml`. Not part of the PR merge gate. See [README.md](../README.md) for invocation details.
