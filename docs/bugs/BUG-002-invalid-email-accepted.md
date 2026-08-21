# BUG-002 — Invalid email format is accepted on create

| Field | Value |
|-------|--------|
| Severity | Major |
| Environment | DEV / PROD |
| Found in | Functional |
| Status | Open |

### Summary

The OpenAPI contract requires `email` to be a valid email string. `POST /users` accepts malformed values (for example `not-an-email`) and returns **201 Created** instead of **400 Bad Request**.

### Steps to reproduce

1. `POST /{env}/users` with body:
   ```json
   { "name": "Jane Doe", "email": "not-an-email", "age": 30 }
   ```

### Expected result

- HTTP **400**
- JSON body `{ "error": "<validation message>" }`

### Actual result

- HTTP **201** (user persisted with invalid email)

### Evidence

- Automated: `CreateUserTests.createUserInvalidEmailReturns400` (Allure `@Issue` link)

### Notes for developers

Validate `email` against standard email format on create (and update). Reject invalid values before persistence.
