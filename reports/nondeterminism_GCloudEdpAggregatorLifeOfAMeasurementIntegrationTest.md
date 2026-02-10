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

### B) `ABORTED: Failed to update because of editVersion mismatch` (solo commit2)
Solo aparece en **commit2**, no en commit3.

En `commit2`:
```
advanceComputation attempt #1 failed; retrying
Caused by: io.grpc.StatusException: ABORTED: Failed to update because of editVersion mismatch.
```

Esto indica que en commit2 se ejecutó el camino de **editVersion mismatch** y el **retry**, mientras que en commit3 no.

## Causa del no determinismo (explicación de bajo nivel)

### 1) EditVersion mismatch → `ABORTED` + retry (solo commit2)
**Qué parte del código lo provoca**
- `GcpSpannerComputationsDatabaseTransactor` compara el `editVersion` del token con el `UpdateTime` actual en Spanner:
  - `src/main/kotlin/org/wfanet/measurement/duchy/deploy/gcloud/spanner/computation/GcpSpannerComputationsDatabaseTransactor.kt`
- Si no coincide, lanza `ComputationTokenVersionMismatchException` (hereda de `DuchyInternalException`):
  - `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/DuchyInternalException.kt`
- Esa excepción se traduce a `Status.ABORTED`:
  - `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computations/ComputationsService.kt`
- El `AsyncComputationControlService` atrapa `ABORTED` como `RetryableException` y reintenta:
  - `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computationcontrol/AsyncComputationControlService.kt`

**Por qué es no determinista**
- Hay **múltiples workers/daemons** procesando computaciones en paralelo.
- Dos workers pueden leer **el mismo token** con `editVersion = X`.
- Uno actualiza primero → `UpdateTime` cambia a `Y`.
- El otro intenta actualizar todavía con `X` → mismatch → `ABORTED`.
- Si el timing no coincide, el mismatch **no ocurre**.

**Resultado:** camino de error + retry ejecutado en algunas corridas (commit2) y no en otras (commit3).

### 2) Log tardío de estado → `FAILED_PRECONDITION` (ambos commits)
**Qué parte del código lo provoca**
- El Duchy envía un log de estado **en cada transición/claim**:
  - `sendStatusUpdateToKingdom` en `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computations/ComputationsService.kt`
- Kingdom rechaza el log si el Measurement ya está en estado terminal (`SUCCEEDED`, `FAILED`, `CANCELLED`):
  - `CreateDuchyMeasurementLogEntry.kt:65-75`
  - `SpannerMeasurementLogEntriesService.kt:63-67`

**Por qué es no determinista**
- El envío del log y la transición a estado terminal ocurren en componentes distintos y **sin sincronización**.
- Si el log llega **antes** del `SUCCEEDED`, se acepta.
- Si llega **después**, se rechaza con `FAILED_PRECONDITION`.
- El orden depende del scheduling y de la concurrencia interna.

## Relación con variación de Codecov

### Explicados por el `ABORTED` (solo commit2)
- `src/main/kotlin/org/wfanet/measurement/duchy/deploy/gcloud/spanner/computation/GcpSpannerComputationsDatabaseTransactor.kt`
- `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/DuchyInternalException.kt`
- `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computationcontrol/AsyncComputationControlService.kt`
- `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computations/ComputationsService.kt`

Estos archivos cambian cobertura si el mismatch ocurre o no.

### No explicados por estos logs
En estos logs **no hay evidencia directa** para:
- `src/main/kotlin/org/wfanet/measurement/common/grpc/ErrorInfo.kt`
- `src/main/kotlin/org/wfanet/measurement/kingdom/service/api/v2alpha/Errors.kt`
- `src/main/kotlin/org/wfanet/measurement/kingdom/service/api/v2alpha/RequisitionsService.kt`

Si esas variaciones aparecen en Codecov, probablemente vienen de otros tests o rutas no logueadas aquí.

## Resumen técnico
- **No determinismo principal**: carreras de concurrencia en actualizaciones de computaciones (editVersion mismatch) y en logs de estado tardíos.
- **Responsables directos**:
  - `GcpSpannerComputationsDatabaseTransactor` (comparación de editVersion)
  - `AsyncComputationControlService` (retry tras ABORTED)
  - `ComputationsService.sendStatusUpdateToKingdom` (log sin sincronizar)
  - `CreateDuchyMeasurementLogEntry` (rechaza logs en estado terminal)
