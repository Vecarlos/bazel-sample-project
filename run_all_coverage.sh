#!/bin/bash

rm -f compile_failed_tests.txt
rm -f tmp.log

# Buscar todos los targets de test bajo src/test/kotlin/org/
echo "🔍 Buscando test targets en src/test/kotlin/org/..."
bazel query 'tests(//src/test/kotlin/org/...)' > all_kotlin_org_tests.txt

while read -r target; do
  echo ">>> Probando cobertura de $target"

  # Ejecutar test de cobertura (silencioso)
  if ! bazel coverage --instrumentation_filter="$target" "$target" > tmp.log 2>&1; then
    if grep -qE "target .* is not visible|Analysis of target .* failed|Build did NOT complete successfully" tmp.log; then
      echo "$target" >> compile_failed_tests.txt
      echo "    ↳ Error de compilación: agregado a compile_failed_tests.txt"
    else
      echo "    ↳ Falló el test, pero compiló bien"
    fi
  else
    echo "    ↳ OK"
  fi
done < all_kotlin_org_tests.txt

rm -f tmp.log
