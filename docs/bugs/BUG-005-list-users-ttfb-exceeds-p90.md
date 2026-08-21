# BUG-005 — GET /users TTFB p90 exceeds 250 ms

| Field | Value |
|-------|--------|
| Severity | Major |
| Environment | DEV / PROD |
| Found in | TTFB |
| Status | Open |

### Summary

Under the challenge TTFB criteria (20 parallel probes, p90 &lt; 250 ms), **GET `/{env}/users`** consistently exceeds the threshold locally. Other user-management endpoints in the same suite typically pass; this endpoint fails repeatably.

### Steps to reproduce

1. Start the User Management API Docker image on `localhost:3000`.
2. Run: `mvn -s settings.xml -Denv=DEV -DxmlFileName=ttfb.xml test`
3. Observe `UsersTtfbTests.listUsersTtfbTest`.

### Expected result

- 20 successful probes
- p90 TTFB **&lt; 250 ms** for `GET /users`

### Actual result

- Probes succeed (no transport failures)
- p90 TTFB is well above 250 ms (observed ~903 ms on a local Docker run)
- Other TTFB tests in the same suite (create / get / update / delete) typically pass under the same conditions

### Evidence

- Automated: `UsersTtfbTests.listUsersTtfbTest` (Allure `@Issue` link)
- Failure message: `GET /users p90 was 903.22 ms, expected < 250 ms`
- Failure is reproducible across repeated local runs for this endpoint only

### Notes for developers

List users appears slower than single-resource operations under concurrent load. Investigate list-handler latency (serialization of the full collection, locking, or cold-path work on every request). Profile `GET /{env}/users` under 20 parallel clients and compare with `GET /{env}/users/{email}`.
