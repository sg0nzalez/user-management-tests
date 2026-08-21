# BUG-003 — GET for missing user returns HTTP 500 instead of 404

| Field | Value |
|-------|--------|
| Severity | Major |
| Environment | DEV / PROD |
| Found in | Functional |
| Status | Open |

### Summary

Looking up a user that does not exist should return **404 Not Found** with an `ErrorResponse`. The API returns **500 Internal Server Error** for unknown emails. The same failure mode may affect **DELETE** for unknown users (see `DeleteUserTests.deleteUserNotFoundReturns404`).

### Steps to reproduce

1. Choose an email that was never created (for example `missing-{uuid}@example.com`).
2. `GET /{env}/users/{email}`

### Expected result

- HTTP **404**
- JSON body `{ "error": "User not found" }` (or equivalent message)

### Actual result

- HTTP **500**
- Server error instead of a structured not-found response

### Evidence

- Automated: `GetUserTests.getUserNotFoundReturns404`, `DeleteUserTests.deleteUserNotFoundReturns404` (Allure `@Issue` link)

### Notes for developers

Treat “user not found” as a domain outcome (404 + `ErrorResponse`), not an unhandled exception. Apply the same handling for GET and DELETE by email path parameter.
