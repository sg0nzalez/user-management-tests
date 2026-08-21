#!/usr/bin/env bash
# Usage: surefire-markdown.sh <label> <surefire-reports-dir>
# Prints a markdown fragment with counts and failing method names.
set -euo pipefail

LABEL="${1:?label required}"
REPORT_DIR="${2:?surefire reports dir required}"

SUMMARY_FILE=""
if [[ -f "${REPORT_DIR}/TestSuite.txt" ]]; then
  SUMMARY_FILE="${REPORT_DIR}/TestSuite.txt"
elif [[ -d "${REPORT_DIR}" ]]; then
  SUMMARY_FILE="$(find "${REPORT_DIR}" -name 'TestSuite.txt' -print -quit 2>/dev/null || true)"
fi

if [[ -z "${SUMMARY_FILE}" || ! -f "${SUMMARY_FILE}" ]]; then
  echo "### ${LABEL}"
  echo
  echo "_No Surefire report found._"
  echo
  exit 0
fi

# Prefer the final suite totals line.
TOTALS_LINE="$(
  grep -E 'Tests run:[[:space:]]*[0-9]+' "${SUMMARY_FILE}" | tail -n 1 || true
)"

TESTS="?"
FAILURES="?"
ERRORS="?"
SKIPPED="?"
if [[ -n "${TOTALS_LINE}" ]]; then
  TESTS="$(sed -n 's/.*Tests run:[[:space:]]*\([0-9][0-9]*\).*/\1/p' <<<"${TOTALS_LINE}")"
  FAILURES="$(sed -n 's/.*Failures:[[:space:]]*\([0-9][0-9]*\).*/\1/p' <<<"${TOTALS_LINE}")"
  ERRORS="$(sed -n 's/.*Errors:[[:space:]]*\([0-9][0-9]*\).*/\1/p' <<<"${TOTALS_LINE}")"
  SKIPPED="$(sed -n 's/.*Skipped:[[:space:]]*\([0-9][0-9]*\).*/\1/p' <<<"${TOTALS_LINE}")"
fi

echo "### ${LABEL}"
echo
echo "| Tests | Failures | Errors | Skipped |"
echo "|------:|---------:|-------:|--------:|"
echo "| ${TESTS} | ${FAILURES} | ${ERRORS} | ${SKIPPED} |"
echo

FAIL_LINES="$(
  grep -E '<<< FAILURE!' "${SUMMARY_FILE}" \
    | sed -E 's/^[[:space:]]*(\[ERROR\][[:space:]]*)?//' \
    | sed -n 's/^\([^[:space:]].*\)[[:space:]]--[[:space:]]*Time elapsed:.*/\1/p' \
    | sed 's/[[:space:]]*$//' \
    | sort -u || true
)"

if [[ -n "${FAIL_LINES}" ]]; then
  echo "**Failures**"
  echo
  while IFS= read -r method; do
    [[ -n "${method}" ]] || continue
    # Skip the suite-level "Tests run: ... <<< FAILURE!" line if captured
    [[ "${method}" == Tests\ run:* ]] && continue
    echo "- \`${method}\`"
  done <<<"${FAIL_LINES}"
  echo
else
  echo "_No failing methods listed._"
  echo
fi
