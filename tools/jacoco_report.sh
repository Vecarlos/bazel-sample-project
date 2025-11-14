#!/usr/bin/env bash
set -euo pipefail

OUTPUT="$1"
shift

# Directorio donde Bazel deja los archivos .exec
EXEC_FILES=("$@")

JACOCO_CLI=$(find . -name "org.jacoco.cli-*.jar" | head -n 1)

if [[ -z "$JACOCO_CLI" ]]; then
  echo "ERROR: JaCoCo CLI JAR not found!"
  exit 1
fi

# Generar XML para Codecov
java -jar "$JACOCO_CLI" report "${EXEC_FILES[@]}" \
  --classfiles . \
  --sourcefiles . \
  --xml "$OUTPUT"

echo "Jacoco XML generated at $OUTPUT"
