# BUG-007 — PUT returns 200 but does not persist updates

| Field | Value |
|-------|--------|
| Severity | Critical |
| Environment | DEV / PROD |
| Found in | Functional |
| Status | Open |

### Summary

`PUT /{env}/users/{email}` responds with **200** and a body that reflects the submitted fields, but a subsequent `GET` still returns the original user. Updates are not persisted.

### Steps to reproduce

1. `POST /{env}/users` with a valid user (note name/age).
2. `PUT /{env}/users/{email}` with a different name and age (same email).
3. Observe the PUT response body shows the new values.
4. `GET /{env}/users/{email}`.

### Expected result

- PUT returns **200** with the updated user
- GET returns **200** with the **same** updated name/age

### Actual result

- PUT returns **200** with the updated-looking body
- GET returns **200** with the **original** name/age unchanged

### Evidence

- Automated: `UpdateUserTests.updateUserIsVisibleOnSubsequentGet`
- Automated: `UserLifecycleTests.createUpdateDeleteLifecycle`
- Confirmed on both `/dev` and `/prod`

### Notes for developers

The update handler appears to serialize the request body as the response without writing the new values to storage. Persist the update (or replace the entity) before returning 200.
