#!/bin/bash


COMMIT_BASE_NAME="no array put"
BRANCH_NAME="buildbuddy"
REMOTE_NAME="origin"
WORKFLOW_NAME=".github/workflows/build-test.yml"
DELAY_SECONDS=440

echo ">>> 1. Creando commit real con 'git add .'"
git add .
git commit -m "$COMMIT_BASE_NAME" && git push $REMOTE_NAME $BRANCH_NAME

for i in {2..7}
do
  echo -e "\n--- Esperando $DELAY_SECONDS segundos para no interrumpir la CI anterior..."
  sleep $DELAY_SECONDS

  echo ">>> $i. Creando commit vacío (p$i)"
  gh workflow run "$WORKFLOW_NAME" --ref "$BRANCH_NAME"
done
echo -e "\n✅ Proceso completado. Se han creado y subido 6 commits."


