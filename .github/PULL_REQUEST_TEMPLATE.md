## Summary

<!-- What changed and why (1–3 bullets). -->

-

## Test plan

- [ ] `mvn -s settings.xml spotless:check`
- [ ] `mvn -s settings.xml pmd:check`
- [ ] `mvn -s settings.xml -Denv=DEV verify` (and/or PROD as relevant)
- [ ] CI `quality` is green
- [ ] Reviewed sticky PR comment / Allure Pages links (if E2E ran)

## Notes

<!-- Bug IDs (e.g. BUG-007), breaking changes, or follow-ups. -->

<!-- Known API mismatches can leave test-dev / test-prod red; that is expected unless this PR intentionally changes assertions. -->
