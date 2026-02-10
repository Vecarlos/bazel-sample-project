# Reporte de no determinismo — GCloudInProcessModelRepositoryCliIntegrationTest

## Contexto
- Test: `//src/test/kotlin/org/wfanet/measurement/integration/deploy/gcloud:GCloudInProcessModelRepositoryCliIntegrationTest`
- Log analizado: `build_logs (11).txt`
- Síntoma observado en el log:
  - `ManagedChannel ... was garbage collected without being shut down`

## Evidencia en log
En `build_logs (11).txt` aparece repetidamente:

```
SEVERE: *~*~*~ Previous channel ManagedChannelImpl{...} was garbage collected without being shut down! ~*~*~*
Make sure to call shutdown()/shutdownNow()
java.lang.RuntimeException: ManagedChannel allocation site
  ...
  at org.wfanet.measurement.common.grpc.ChannelKt.buildMutualTlsChannel(Channel.kt:48)
  at org.wfanet.measurement.integration.common.InProcessModelRepositoryCliIntegrationTest.startServer(InProcessModelRepositoryCliIntegrationTest.kt:270)
```

Ejemplo concreto en `build_logs (11).txt` líneas 914–926.

## Causa real (código)

### 1) Se crean ManagedChannel pero no se cierran
Archivo:
- `src/main/kotlin/org/wfanet/measurement/integration/common/InProcessModelRepositoryCliIntegrationTest.kt`

En `startServer()`:
- Se crea un channel para model provider:
  - `buildMutualTlsChannel("localhost:${server.port}", modelProviderCerts)` (`InProcessModelRepositoryCliIntegrationTest.kt:270`)
- Se crea un channel para data provider:
  - `buildMutualTlsChannel("localhost:${server.port}", dataProviderCerts)` (`InProcessModelRepositoryCliIntegrationTest.kt:287`)

### 2) En el teardown solo se cierra el server
Archivo:
- `src/main/kotlin/org/wfanet/measurement/integration/common/InProcessModelRepositoryCliIntegrationTest.kt`

En `@After shutdownServer()`:
```kotlin
server.close()
```
No hay `shutdown()` de los `ManagedChannel` creados en `startServer()`.

## Por qué es no determinista
El warning aparece **cuando el GC detecta channels no cerrados**. El momento exacto
en el que el GC recolecta esos objetos es variable → por eso el log cambia entre corridas.

No es un race funcional como en los otros tests, pero sí es **comportamiento no determinista
por recursos sin liberar**.

## Resumen técnico
- **Causa**: `ManagedChannel` creado y no cerrado en `startServer()`.
- **Lugar exacto**:
  - Creación: `InProcessModelRepositoryCliIntegrationTest.kt:270` y `:287`
  - Falta de cierre: `shutdownServer()` solo hace `server.close()`.
- **Efecto**: warning `ManagedChannel ... was garbage collected without being shut down`.
- **Indeterminismo**: depende del timing del GC.
