# BUG-008 — Non-JSON Content-Type is accepted on POST /users

| Field | Value |
|-------|--------|
| Severity | Minor |
| Environment | DEV / PROD |
| Found in | Functional |
| Status | Open |

### Summary

The OpenAPI request body for `POST /users` declares `application/json`. Sending the same JSON bytes with `Content-Type: text/plain` should be rejected (**415 Unsupported Media Type**). The API accepts the request and returns **201**.

### Steps to reproduce

1. `POST /{env}/users` with header `Content-Type: text/plain` and a JSON body `{"name":"...","email":"...","age":30}`.

### Expected result

- HTTP **415**

### Actual result

- HTTP **201** and the user is created

### Evidence

- Automated: `CreateUserTests.createUserWrongContentTypeReturns415`

### Notes for developers

Enforce `Content-Type: application/json` (or negotiate explicitly) before deserializing the body.
