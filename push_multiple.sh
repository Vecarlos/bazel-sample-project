#!/bin/bash


COMMIT_BASE_NAME="all collect all, cache and write coverage"
BRANCH_NAME="buildbuddy"
REMOTE_NAME="origin"

DELAY_SECONDS=160

echo ">>> 1. Creando commit real con 'git add .'"
git add .
git commit -m "$COMMIT_BASE_NAME" && git push $REMOTE_NAME $BRANCH_NAME

for i in {2..6}
do
  echo -e "\n--- Esperando $DELAY_SECONDS segundos para no interrumpir la CI anterior..."
  sleep $DELAY_SECONDS

  echo ">>> $i. Creando commit vacío (p$i)"
  git commit --allow-empty -m "$COMMIT_BASE_NAME p$i" && git push $REMOTE_NAME $BRANCH_NAME
done
echo -e "\n✅ Proceso completado. Se han creado y subido 6 commits."
