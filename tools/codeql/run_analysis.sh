#!/bin/bash
set -e

LANGUAGE="$LANGUAGE"
CODEQL_EXEC="./external/_main~_repo_rules~codeql_cli/codeql"

echo "--- Creating CodeQL database for ${LANGUAGE} by tracing the Bazel build ---"
"${CODEQL_EXEC}" database create \
  --language="${LANGUAGE}" \
  --command="bazel build \
    --config=ci \
    --spawn_strategy=local \
    //..." \
  codeql-db

echo "--- Analyzing database ---"
"${CODEQL_EXEC}" database analyze --format=sarif-latest --output=results.sarif codeql-db

echo "--- CodeQL analysis finished successfully! ---"