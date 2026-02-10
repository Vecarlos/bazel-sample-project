# Reporte de no determinismo — GCloudInProcessLifeOfAReportV2IntegrationTest

## Contexto
- Test: `//src/test/kotlin/org/wfanet/measurement/integration/deploy/gcloud:GCloudInProcessLifeOfAReportV2IntegrationTest`
- Log analizado: `build_logs (10).txt`
- Síntomas observados en el log:
  - `FAILED_PRECONDITION: Measurement in wrong state. state=SUCCEEDED`
  - `FAILED_PRECONDITION: ContinuationToken to set cannot have older timestamp.`

## Evidencia en log (extractos)

### A) Log tardío de estado de Measurement
Ejemplo en `build_logs (10).txt`:
```
WARNING: Failed to update status change to the kingdom. io.grpc.StatusException: FAILED_PRECONDITION: Measurement in wrong state. state=SUCCEEDED
```

Aparece repetidamente (p.ej. líneas ~9329, ~151412, ~160661, ~170148, etc.).

### B) Continuation token fuera de orden
Ejemplo en `build_logs (10).txt`:
```
WARNING: Failure happened during setContinuationToken
io.grpc.StatusException: FAILED_PRECONDITION: ContinuationToken to set cannot have older timestamp.
```

Aparece cerca de las líneas ~95793–95795.

## Causa 1: `Measurement in wrong state` (race de estados)

### Flujo exacto en código
1) Duchy envía log de estado:
- `src/main/kotlin/org/wfanet/measurement/duchy/deploy/common/postgres/PostgresComputationsService.kt:479`

```kotlin
private suspend fun sendStatusUpdateToKingdom(request: CreateComputationLogEntryRequest) {
  try {
    computationLogEntriesClient.createComputationLogEntry(request)
  } catch (ignored: Exception) {
    logger.log(Level.WARNING, "Failed to update status change to the kingdom. $ignored")
  }
}
```

2) Este método se dispara desde transiciones reales:
- `claimWork` → `PostgresComputationsService.kt:192`
- `finishComputation` → `PostgresComputationsService.kt:299`
- `advanceComputationStage` → `PostgresComputationsService.kt:401`

3) Kingdom rechaza si ya está terminal:
- `CreateDuchyMeasurementLogEntry.kt:65-75` → lanza `MeasurementStateIllegalException`
- `SpannerMeasurementLogEntriesService.kt:63-67` → `FAILED_PRECONDITION` con “Measurement in wrong state”

### Por qué es no determinista
Los daemons y mills corren en paralelo. Dependiendo del orden real de ejecución:
- Si el log llega antes del `SUCCEEDED` → OK.
- Si llega después → `FAILED_PRECONDITION`.

Eso hace que la ruta de error se ejecute intermitentemente, cambiando coverage.

## Causa 2: `ContinuationToken` fuera de orden (race de tokens)

### Flujo exacto en código
1) Herald marca un token como procesado y lo intenta setear:
- `src/main/kotlin/org/wfanet/measurement/duchy/herald/ContinuationTokenManager.kt:100-109`

```kotlin
try {
  continuationTokenClient.setContinuationToken(setRequest)
} catch (e: StatusException) {
  if (e.status.code == Status.Code.FAILED_PRECONDITION) {
    logger.log(Level.WARNING, e) { "Failure happened during setContinuationToken" }
  } else {
    throw SetContinuationTokenException(...)
  }
}
```

2) Validación en Postgres (rechaza tokens viejos):
- `src/main/kotlin/org/wfanet/measurement/duchy/deploy/common/postgres/writers/SetContinutationToken.kt:71-81`

```kotlin
if (oldContinuationToken != null &&
    newContinuationToken.lastSeenUpdateTime < oldContinuationToken.lastSeenUpdateTime) {
  throw ContinuationTokenInvalidException(...)
}
```

3) La excepción se traduce a gRPC FAILED_PRECONDITION:
- `src/main/kotlin/org/wfanet/measurement/duchy/deploy/common/postgres/PostgresContinuationTokensService.kt:45-52`

### Por qué es no determinista
`ContinuationTokenManager` asume que los tokens llegan en orden, pero el procesamiento
puede completarse fuera de orden por concurrencia, entonces intenta setear un token
más viejo → `FAILED_PRECONDITION`.

## Resumen técnico
- **Causa 1 (estado SUCCEEDED)**: log tardío de transición de computación → Kingdom rechaza
  si el Measurement ya terminó.
- **Causa 2 (continuation token)**: tokens procesados fuera de orden → Postgres rechaza token viejo.

Ambas causas son carreras de timing y aparecen intermitentemente, lo cual genera variación en coverage.
