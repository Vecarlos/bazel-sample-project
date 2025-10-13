#!/bin/bash

# --- CONFIGURACIÓN ---
# Cambia esta variable para el nombre base de tus commits
COMMIT_BASE_NAME="Solver  init self._add_equals"

# Nombre de la rama a la que quieres subir los cambios
BRANCH_NAME="buildbuddy"
REMOTE_NAME="origin"

# Tiempo de espera en segundos (1 minuto y 20 segundos = 80 segundos)
DELAY_SECONDS=60
# --- FIN DE LA CONFIGURACIÓN ---


# 1. Commit real con los cambios actuales
echo ">>> 1. Creando commit real con 'git add .'"
git add .
git commit -m "$COMMIT_BASE_NAME" && git push $REMOTE_NAME $BRANCH_NAME


# 2. Bucle para los 5 commits vacíos (del 2 al 6)
for i in {2..5}
do
  echo -e "\n--- Esperando $DELAY_SECONDS segundos para no interrumpir la CI anterior..."
  sleep $DELAY_SECONDS

  echo ">>> $i. Creando commit vacío (p$i)"
  git commit --allow-empty -m "$COMMIT_BASE_NAME p$i" && git push $REMOTE_NAME $BRANCH_NAME
done

echo -e "\n✅ Proceso completado. Se han creado y subido 6 commits."
