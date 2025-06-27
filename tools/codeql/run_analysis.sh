#!/bin/bash
set -e

LANGUAGE="$LANGUAGE"
CODEQL_EXEC="./external/_main~_repo_rules~codeql_cli/codeql"

"${CODEQL_EXEC}" database create \
  --language="${LANGUAGE}" \
  --command="bazel build --spawn_strategy=local //..." \
  codeql-db

# El paso "echo '--- Building code...' " y "bazel build..." que tenías antes
# ya no son necesarios, porque ahora CodeQL ejecuta el build por nosotros.

echo "--- Analyzing database ---"
"${CODEQL_EXEC}" database analyze --format=sarif-latest --output=results.sarif codeql-db

echo "--- CodeQL analysis finished successfully! ---"