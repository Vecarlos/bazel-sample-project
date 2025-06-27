#!/bin/bash
set -e

LANGUAGE="$LANGUAGE"

# La dependencia @codeql_cli se encuentra en la carpeta external/.
# Esta es la ruta correcta y robusta dentro del entorno del test.
CODEQL_EXEC="./external/_main~_repo_rules~codeql_cli/codeql"

echo "--- Initializing CodeQL for ${LANGUAGE} ---"
"${CODEQL_EXEC}" database create --language="${LANGUAGE}" codeql-db

echo "--- Building code for CodeQL analysis ---"
# OJO: Asumimos que 'bazel' está disponible en el contenedor de RBE.
# Las imágenes de RBE de BuildBuddy suelen tenerlo.
bazel build --spawn_strategy=local -- //...

echo "--- Analyzing with CodeQL ---"
"${CODEQL_EXEC}" database analyze --format=sarif-latest --output=results.sarif codeql-db