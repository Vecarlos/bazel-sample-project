# Reporte de no determinismo — GCloudEdpAggregatorLifeOfAMeasurementIntegrationTest

## Contexto
- Test: `//src/test/kotlin/org/wfanet/measurement/integration/deploy/gcloud:GCloudEdpAggregatorLifeOfAMeasurementIntegrationTest`
- Logs comparados:
  - `GCloudEdpAggregatorLifeOfAMeasurementIntegrationTest_commit2.txt`
  - `GCloudEdpAggregatorLifeOfAMeasurementIntegrationTest_commit3.txt`

## Variación observada entre commits (evidencia en logs)

### A) `FAILED_PRECONDITION: Measurement in wrong state. state=SUCCEEDED`
Aparece en **ambos** logs (commit2 y commit3), por lo tanto **no explica diferencias** entre ellos, pero sí explica variación general de coverage.

- commit2: líneas ~17652 y ~25318
- commit3: líneas ~17545 y ~24490

Esto corresponde al warning:
```
Failed to update status change to the kingdom.
io.grpc.StatusException: FAILED_PRECONDITION: Measurement in wrong state. state=SUCCEEDED
```

### B) `ABORTED: Failed to update because of editVersion mismatch` (solo commit2)
Solo aparece en **commit2**, no en commit3.

En `commit2`:
```
advanceComputation attempt #1 failed; retrying
Caused by: io.grpc.StatusException: ABORTED: Failed to update because of editVersion mismatch.
```

Esto indica que en commit2 se ejecutó el camino de **editVersion mismatch** y el **retry**, mientras que en commit3 no.

## Ruta de código exacta (bajo nivel)

### 1) Log tardío de estado → `FAILED_PRECONDITION` (ambos commits)
- Envío del log (Duchy):
  - `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computations/ComputationsService.kt:409`
- Validación y rechazo (Kingdom):
  - `src/main/kotlin/org/wfanet/measurement/kingdom/deploy/gcloud/spanner/writers/CreateDuchyMeasurementLogEntry.kt:65-75`
  - `src/main/kotlin/org/wfanet/measurement/kingdom/deploy/gcloud/spanner/SpannerMeasurementLogEntriesService.kt:63-67`

**Causa del no determinismo**: el log llega después de que el Measurement ya está en `SUCCEEDED`.

### 2) EditVersion mismatch → `ABORTED` + retry (solo commit2)
- Comparación de versión y throw:
  - `src/main/kotlin/org/wfanet/measurement/duchy/deploy/gcloud/spanner/computation/GcpSpannerComputationsDatabaseTransactor.kt`
- Excepción específica:
  - `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/DuchyInternalException.kt`
  - `ComputationTokenVersionMismatchException`
- Traducción a `ABORTED`:
  - `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computations/ComputationsService.kt`
- Retry:
  - `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computationcontrol/AsyncComputationControlService.kt`

**Causa del no determinismo**: dos escrituras concurrentes con `editVersion` distinto → una corrida entra en mismatch/ABORTED, otra no.

## Relación con variación de Codecov
Los cambios de coverage observados en Codecov (ej. commit `21a15ab` vs `031a27d`) son **congruentes** con la presencia o ausencia del camino de `ABORTED`:

### Explicados por el `ABORTED` (solo commit2)
- `src/main/kotlin/org/wfanet/measurement/duchy/deploy/gcloud/spanner/computation/GcpSpannerComputationsDatabaseTransactor.kt`
- `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/DuchyInternalException.kt`
- `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computationcontrol/AsyncComputationControlService.kt`
- `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computations/ComputationsService.kt`

Estos archivos suben/bajan cobertura dependiendo de si el mismatch se ejecuta.

### No explicados por estos logs
En los logs de este test **no hay evidencia directa** para:
- `src/main/kotlin/org/wfanet/measurement/common/grpc/ErrorInfo.kt`
- `src/main/kotlin/org/wfanet/measurement/kingdom/service/api/v2alpha/Errors.kt`
- `src/main/kotlin/org/wfanet/measurement/kingdom/service/api/v2alpha/RequisitionsService.kt`

Si esas variaciones aparecen en Codecov, probablemente vienen de otros tests o de rutas no logueadas en estos archivos.

## Resumen técnico
- **Determinístico**: el test ejecuta siempre la lógica principal.
- **No determinístico**:
  - Log tardío de estado (`FAILED_PRECONDITION: Measurement in wrong state`) → aparece siempre pero en distintos puntos.
  - Mismatch de `editVersion` → aparece solo en algunas corridas (commit2 sí, commit3 no), causando cambios claros en coverage.
