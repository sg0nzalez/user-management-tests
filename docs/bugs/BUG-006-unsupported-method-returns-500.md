# BUG-006 — Unsupported HTTP methods return 500 instead of 405

| Field | Value |
|-------|--------|
| Severity | Major |
| Environment | DEV / PROD |
| Found in | Functional |
| Status | Open |

### Summary

Paths document only specific verbs (`GET`/`POST` on `/users`; `GET`/`PUT`/`DELETE` on `/users/{email}`). Requests using other verbs should return **405 Method Not Allowed**. The API responds with **500 Internal Server Error** instead.

### Steps to reproduce

1. `PUT`, `PATCH`, or `DELETE` against `/{env}/users` (with or without a JSON body).
2. `POST` or `PATCH` against `/{env}/users/{email}` for any email.

### Expected result

- HTTP **405**
- Ideally an `Allow` header listing the supported methods for that path

### Actual result

- HTTP **500**
- JSON body `{ "error": "Internal server error" }`

### Evidence

- Automated:
  - `CreateUserTests.createUserOnResourcePathReturns405`
  - `UpdateUserTests.updateUserOnCollectionPathReturns405`
  - `UpdateUserTests.patchUserOnCollectionPathReturns405`
  - `UpdateUserTests.patchUserOnResourcePathReturns405`
  - `DeleteUserTests.deleteUserOnCollectionPathReturns405`

### Notes for developers

Reject unsupported verbs at the router/framework layer before handler execution so clients receive **405** rather than an unhandled exception.
