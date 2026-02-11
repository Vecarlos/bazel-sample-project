# Reporte de no determinismo — GCloudEdpAggregatorLifeOfAMeasurementIntegrationTest

## Contexto
- Test: `//src/test/kotlin/org/wfanet/measurement/integration/deploy/gcloud:GCloudEdpAggregatorLifeOfAMeasurementIntegrationTest`
- Logs comparados:
  - `GCloudEdpAggregatorLifeOfAMeasurementIntegrationTest_commit2.txt`
  - `GCloudEdpAggregatorLifeOfAMeasurementIntegrationTest_commit3.txt`

## Diferencias observadas en logs (commit2 vs commit3)

### A) Log tardío de estado en Kingdom (aparece en ambos)
En ambos logs aparece:
```
Failed to update status change to the kingdom.
FAILED_PRECONDITION: Measurement in wrong state. state=SUCCEEDED
```

Esto es no determinista a nivel de timing, pero **no explica diferencias entre commits** porque ocurre en ambos.

### B) EditVersion mismatch + retry (solo commit2)
En `commit2` aparece:
```
advanceComputation attempt #1 failed; retrying
Caused by: io.grpc.StatusException: ABORTED: Failed to update because of editVersion mismatch.
```
En `commit3` **no aparece**.

## Causas de no determinismo (bajo nivel)

### 1) Log tardío de estado → `FAILED_PRECONDITION`
**Responsable:** el Duchy envía un log de estado en cada transición sin coordinar con el estado terminal.

- Envío del log:
  - `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computations/ComputationsService.kt`
  - `sendStatusUpdateToKingdom()`
- Validación y rechazo:
  - `src/main/kotlin/org/wfanet/measurement/kingdom/deploy/gcloud/spanner/writers/CreateDuchyMeasurementLogEntry.kt`
  - `src/main/kotlin/org/wfanet/measurement/kingdom/deploy/gcloud/spanner/SpannerMeasurementLogEntriesService.kt`

**Por qué varía:**
El log puede llegar antes o después de que el Measurement esté `SUCCEEDED`. Ese orden depende del scheduling de daemons.

### 2) EditVersion mismatch → `ABORTED` + retry (solo commit2)
**Responsable:** control de concurrencia optimista en updates de computación.

- Comparación de versiones y throw:
  - `src/main/kotlin/org/wfanet/measurement/duchy/deploy/common/postgres/writers/ComputationMutations.kt`
  - `src/main/kotlin/org/wfanet/measurement/duchy/deploy/gcloud/spanner/computation/GcpSpannerComputationsDatabaseTransactor.kt`
- Excepción específica:
  - `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/DuchyInternalException.kt`
- Traducción a `ABORTED`:
  - `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computations/ComputationsService.kt`
- Retry:
  - `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computationcontrol/AsyncComputationControlService.kt`

**Por qué varía:**
Dos workers pueden actualizar la misma computation con el mismo `editVersion`. Si uno actualiza primero, el otro queda con versión vieja y falla. Solo ocurre cuando el timing coincide.

## Relación con variación de Codecov
Los archivos que varían en Codecov para este test son consistentes con lo observado en `commit2`:

- `GcpSpannerComputationsDatabaseTransactor.kt`, `ComputationMutations.kt` → editVersion mismatch.
- `DuchyInternalException.kt`, `ComputationsService.kt`, `AsyncComputationControlService.kt` → ABORTED y retry.

Otros archivos (p.ej. `ErrorInfo.kt`, `Errors.kt`, `RequisitionsService.kt`) **no están respaldados por estos logs** y probablemente vienen de otros tests.

## Cómo arreglar el no determinismo (opciones)

### Opción A: Reducir concurrencia en tests
- Bajar `DUCHY_MILL_PARALLELISM` a 1 para evitar colisiones de `editVersion`.

### Opción B: Tolerar log tardío
- Evitar que `CreateDuchyMeasurementLogEntry` falle cuando el Measurement está terminal (o mover el log a un punto sincronizado).

### Opción C: Control de versiones más robusto
- Usar versión monotónica (no basada en reloj) o serializar updates críticos.

Cada opción reduce rutas de error intermitentes y estabiliza coverage.
