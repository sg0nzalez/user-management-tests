# BUG-001 — Duplicate email returns HTTP 500 instead of 409

| Field | Value |
|-------|--------|
| Severity | Major |
| Environment | DEV / PROD |
| Found in | Functional |
| Status | Open |

### Summary

Creating a user with an email that already exists should return **409 Conflict** per the OpenAPI spec. The API responds with **500 Internal Server Error** instead.

### Steps to reproduce

1. `POST /{env}/users` with a valid user payload (note the email).
2. Repeat `POST /{env}/users` with the same email and any name/age.

### Expected result

- HTTP **409**
- JSON body `{ "error": "<message>" }`

### Actual result

- HTTP **500**
- Server error response (not the documented conflict shape)

### Evidence

- Automated: `CreateUserTests.createUserDuplicateEmailReturns409` (Allure `@Issue` link)
- Request: duplicate `email` field on second POST

### Notes for developers

Handle unique constraint violations on `email` (primary key) and map them to **409** with an `ErrorResponse` body, not an unhandled exception.
