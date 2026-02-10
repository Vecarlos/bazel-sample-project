# Reporte de no determinismo — GCloudInProcessLifeOfAReportV2IntegrationTest

## Contexto
- Test: `//src/test/kotlin/org/wfanet/measurement/integration/deploy/gcloud:GCloudInProcessLifeOfAReportV2IntegrationTest`
- Logs comparados:
  - `GCloudInProcessLifeOfAReportV2IntegrationTest_commit2.txt`
  - `GCloudInProcessLifeOfAReportV2IntegrationTest_commit3.txt`

## Diferencias observadas en logs (commit2 vs commit3)

### A) Log tardío de estado en Kingdom (aparece en ambos)
En ambos logs aparece repetidamente:
```
Failed to update status change to the kingdom.
FAILED_PRECONDITION: Measurement in wrong state. state=SUCCEEDED
```
Esto es no determinista a nivel de timing, pero **no explica la variación entre commits**, porque ocurre en ambos.

### B) EditVersion mismatch + retry (solo commit3)
En `commit3` aparece:
```
advanceComputation attempt #1 failed; retrying
Caused by: io.grpc.StatusException: ABORTED: Failed to update because of editVersion mismatch.
```
En `commit2` **no aparece** este bloque.

### C) Continuation token fuera de orden (solo commit3)
En `commit3` aparece:
```
Failure happened during setContinuationToken
FAILED_PRECONDITION: ContinuationToken to set cannot have older timestamp.
```
En `commit2` **no aparece** este bloque.

## Causas de no determinismo (bajo nivel)

### 1) Log tardío de estado → `FAILED_PRECONDITION`
**Responsable:** log de estado enviado desde Duchy sin sincronización con el estado terminal del Measurement.

- Envío del log:
  - `src/main/kotlin/org/wfanet/measurement/duchy/deploy/common/postgres/PostgresComputationsService.kt`
  - `sendStatusUpdateToKingdom()`
- Validación y rechazo en Kingdom:
  - `src/main/kotlin/org/wfanet/measurement/kingdom/deploy/gcloud/spanner/writers/CreateDuchyMeasurementLogEntry.kt`
  - `src/main/kotlin/org/wfanet/measurement/kingdom/deploy/gcloud/spanner/SpannerMeasurementLogEntriesService.kt`

**Por qué varía:**
El log llega antes o después del `SUCCEEDED` según el timing de los daemons. Ese orden no está sincronizado.

### 2) EditVersion mismatch → `ABORTED` + retry (solo commit3)
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
Dos workers pueden actualizar la misma computation con el mismo `editVersion`. Si uno actualiza primero, el otro queda con versión vieja y falla. Esto ocurre solo cuando el timing coincide.

### 3) Continuation token fuera de orden → `FAILED_PRECONDITION` (solo commit3)
**Responsable:** tokens procesados fuera de orden temporal.

- Escritura del token (rechaza tokens viejos):
  - `src/main/kotlin/org/wfanet/measurement/duchy/deploy/common/postgres/writers/SetContinutationToken.kt`
- RPC que traduce el error:
  - `src/main/kotlin/org/wfanet/measurement/duchy/deploy/common/postgres/PostgresContinuationTokensService.kt`
- Warning al setear:
  - `src/main/kotlin/org/wfanet/measurement/duchy/herald/ContinuationTokenManager.kt`

**Por qué varía:**
`ContinuationTokenManager` asume orden temporal, pero el procesamiento puede completarse en distinto orden por concurrencia, generando tokens “viejos”.

## Relación con variación de Codecov
Los archivos que varían en Codecov para este test son consistentes con lo que aparece en `commit3` pero no en `commit2`:

- `ComputationMutations.kt`, `GcpSpannerComputationsDatabaseTransactor.kt` → explican el `editVersion mismatch`.
- `DuchyInternalException.kt`, `ComputationsService.kt`, `AsyncComputationControlService.kt` → explican `ABORTED` y retry.
- `SetContinutationToken.kt`, `PostgresContinuationTokensService.kt`, `ContinuationTokenManager.kt` → explican el `FAILED_PRECONDITION` de tokens.
- `PostgresComputationsService.kt` → log tardío de estado.

Esto explica la variación de coverage entre commits.

## Cómo arreglar el no determinismo (opciones)

### Opción A: Reducir concurrencia en tests
- Bajar `DUCHY_MILL_PARALLELISM` a 1 en tests para evitar colisiones de `editVersion` y orden de tokens.

### Opción B: Hacer tolerante el log tardío de estado
- Evitar que `CreateDuchyMeasurementLogEntry` falle cuando el Measurement está terminal, o mover el log a un punto sincronizado.

### Opción C: Hacer el control de versiones más robusto
- Usar una versión monotónica independiente del clock, o serializar updates críticos para evitar mismatch.

### Opción D: Ordenar/ignorar tokens viejos
- En `ContinuationTokenManager`, evitar enviar tokens más viejos (buffer y ordenar) o tratar ese `FAILED_PRECONDITION` como no‑op.

Cada opción reduce rutas de error intermitentes y estabiliza coverage.
