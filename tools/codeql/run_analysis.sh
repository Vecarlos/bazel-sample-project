#!/bin/bash
set -e

# --- COMIENZO DE LA SECCIÓN DE DEPURACIÓN ---
echo "--- DEBUGGING RBE ENVIRONMENT ---"
echo "Current working directory:"
pwd
echo
echo "Listing all available files recursively:"
# ls -lR listará todos los archivos y carpetas, mostrándonos dónde está todo.
ls -lR
echo "--- END OF DEBUGGING ---"
# --- FIN DE LA SECCIÓN DE DEPURACIÓN ---


LANGUAGE="$LANGUAGE"
CODEQL_EXEC="external/codeql_cli/codeql" # Dejamos esto por ahora

echo "--- Initializing CodeQL for ${LANGUAGE} ---"
"${CODEQL_EXEC}" database create --language="${LANGUAGE}" codeql-db

echo "--- Building code for RBE analysis ---"
bazel build --spawn_strategy=local -- //...

echo "--- Analyzing with CodeQL ---"
"${CODEQL_EXEC}" database analyze --format=sarif-latest --output=results.sarif codeql-db