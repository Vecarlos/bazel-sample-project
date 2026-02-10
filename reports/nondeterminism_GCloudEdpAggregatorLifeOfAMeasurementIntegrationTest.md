# Reporte de no determinismo — GCloudEdpAggregatorLifeOfAMeasurementIntegrationTest

## Contexto
- Test: `//src/test/kotlin/org/wfanet/measurement/integration/deploy/gcloud:GCloudEdpAggregatorLifeOfAMeasurementIntegrationTest`
- Log analizado: `build_logs (9).txt`
- Síntoma observado en el log: warnings de gRPC con `FAILED_PRECONDITION: Measurement in wrong state. state=SUCCEEDED`.

## Evidencia en log
En `build_logs (9).txt` aparecen warnings como:
```
WARNING: Failed to update status change to the kingdom.
io.grpc.StatusException: FAILED_PRECONDITION: Measurement in wrong state. state=SUCCEEDED
```

Ejemplos de contexto (ubicaciones aproximadas en el log):
- Cerca de las líneas ~17588
- Cerca de las líneas ~24461

## Flujo exacto en código (ruta de ejecución)

### 1) Duchy intenta registrar log de estado
Archivo:
- `src/main/kotlin/org/wfanet/measurement/duchy/service/internal/computations/ComputationsService.kt`

Método (envía el log y registra el warning si falla):
- `sendStatusUpdateToKingdom` (`ComputationsService.kt:409`)

```kotlin
private suspend fun sendStatusUpdateToKingdom(request: CreateComputationLogEntryRequest) {
  try {
    computationLogEntriesClient.createComputationLogEntry(request)
  } catch (e: StatusException) {
    logger.log(Level.WARNING, e) { "Failed to update status change to the kingdom." }
  }
}
```

### 2) Este método se llama desde transiciones de computación
Mismo archivo `ComputationsService.kt`:
- `claimWork` → `sendStatusUpdateToKingdom` (`ComputationsService.kt:102`)
- `createComputation` → (`ComputationsService.kt:147`)
- `advanceComputationStage` → (`ComputationsService.kt:329`)
- `finishComputation` → (`ComputationsService.kt:231`)

Eso significa que **cada transición o claim** intenta escribir un log en Kingdom.

### 3) Kingdom valida el estado y rechaza si ya está terminal
Validación en:
- `src/main/kotlin/org/wfanet/measurement/kingdom/deploy/gcloud/spanner/writers/CreateDuchyMeasurementLogEntry.kt:65-75`

```kotlin
when (measurementInfo.measurementState) {
  Measurement.State.SUCCEEDED,
  Measurement.State.FAILED,
  Measurement.State.CANCELLED ->
    throw MeasurementStateIllegalException(...)
  // estados pendientes permiten log
}
```

Transformación a gRPC error:
- `src/main/kotlin/org/wfanet/measurement/kingdom/deploy/gcloud/spanner/SpannerMeasurementLogEntriesService.kt:63-67`

```kotlin
throw e.asStatusRuntimeException(
  Status.Code.FAILED_PRECONDITION,
  "Measurement in wrong state. state=${e.state}",
)
```

## Causa real del no determinismo
El log se envía **sin sincronizar** con el momento exacto en que el Measurement pasa a estado terminal en Kingdom.

En algunas corridas:
- el log llega **antes** del `SUCCEEDED` → no hay error.

En otras corridas:
- el log llega **después** → Kingdom lo rechaza con `FAILED_PRECONDITION`.

Esto es un **race de timing** entre:
- los daemons y mills del Duchy,
- y la transición final del estado en Kingdom.

## Por qué ese race existe en este test
El test levanta varios componentes concurrentes:

Archivo:
- `src/main/kotlin/org/wfanet/measurement/integration/common/InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest.kt`

En `@Before setup()`:
- `inProcessCmmsComponents.startDaemons()`
- `inProcessEdpAggregatorComponents.startDaemons(...)`

Además, los mills trabajan en paralelo con:
- `DUCHY_MILL_PARALLELISM = 3` (`src/main/kotlin/org/wfanet/measurement/integration/common/Configs.kt:155`)
- Usado en `InProcessDuchy` (`src/main/kotlin/org/wfanet/measurement/integration/common/InProcessDuchy.kt:270-301`)

Esto introduce **concurrencia real** y, por ende, orden variable de eventos.

## Resumen técnico
- **No determinismo real**: logs de estado llegan tarde.
- **Punto exacto del error**: `CreateDuchyMeasurementLogEntry.kt:65-75` → `FAILED_PRECONDITION`.
- **Origen**: `sendStatusUpdateToKingdom` se ejecuta en cada transición sin verificar el estado terminal.
- **Condición**: concurrencia y orden variable por daemons/mills paralelos.

## Impacto
Estas rutas de error (y sus clases) se ejecutan **de forma intermitente**, lo cual altera qué líneas se consideran cubiertas en cada corrida de `bazel coverage`.
