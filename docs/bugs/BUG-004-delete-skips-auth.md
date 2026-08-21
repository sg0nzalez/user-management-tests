# BUG-004 — DELETE succeeds without valid Authentication (DEV only)

| Field | Value |
|-------|--------|
| Severity | Critical |
| Environment | DEV only (PROD not affected) |
| Found in | Functional |
| Status | Open |

### Summary

The OpenAPI spec marks the `Authentication` header as **required** on `DELETE /users/{email}` and documents **401** when it is missing or invalid. On **DEV**, the API deletes users when the header is omitted, empty, wrong, or when a different header name (`Authorization`) is sent instead. On **PROD**, those requests correctly return **401**.

### Steps to reproduce

1. Create a user via `POST /dev/users`.
2. `DELETE /dev/users/{email}` with any of:
   - no `Authentication` header
   - `Authentication: invalid-token`
   - `Authentication:` (empty value)
   - `Authorization: Bearer <token>` (wrong header name)
3. Optionally repeat against `/prod/users` — those should return 401.

### Expected result

- HTTP **401**
- JSON body `{ "error": "<message>" }`
- User remains in the database

### Actual result

- **DEV:** HTTP **204 No Content**; user is removed without valid credentials
- **PROD:** HTTP **401** (spec-correct; auth tests pass)

### Evidence

- Automated (fail on DEV, pass on PROD):
  - `DeleteUserTests.deleteUserWithoutTokenReturns401`
  - `DeleteUserTests.deleteUserWithInvalidTokenReturns401`
  - `DeleteUserTests.deleteUserWithEmptyTokenReturns401`
  - `DeleteUserTests.deleteUserWithWrongHeaderNameReturns401`

### Notes for developers

DEV DELETE appears to skip (or inconsistently apply) authentication middleware that PROD already enforces. Align DEV with PROD: reject missing, empty, or invalid `Authentication` before performing the delete. Do not accept `Authorization` in place of the documented `Authentication` header.
