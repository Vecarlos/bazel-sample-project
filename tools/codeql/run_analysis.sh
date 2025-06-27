#!/bin/bash
set -e # Falla si cualquier comando falla

# El lenguaje (cpp, java-kotlin) se pasará como un argumento.

echo "--- Initializing CodeQL for ${LANGUAGE} ---"
# NOTA: Los paths a CodeQL pueden variar dependiendo de tu contenedor RBE.
# Este es un ejemplo.
/path/to/codeql/codeql database create --language="${LANGUAGE}" codeql-db

echo "--- Building code for CodeQL analysis ---"
# Forzamos la ejecución local DENTRO del worker de RBE para que CodeQL vea la compilación.
bazel build --spawn_strategy=local -- //...

echo "--- Analyzing with CodeQL ---"
/path/to/codeql/codeql database analyze --format=sarif-latest --output=results.sarif codeql-db