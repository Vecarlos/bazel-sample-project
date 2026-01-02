// Copyright 2020 The Cross-Media Measurement Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.wfanet.measurement.duchy.service.internal.computations

import java.time.Clock
import java.time.Duration
import java.util.logging.Logger
import kotlinx.coroutines.runBlocking
import org.wfanet.measurement.common.toProtoTime
import org.wfanet.measurement.duchy.toProtocolStage
import org.wfanet.measurement.internal.duchy.ComputationsGrpcKt.ComputationsCoroutineStub
import org.wfanet.measurement.internal.duchy.protocol.LiquidLegionsSketchAggregationV2.Stage
import org.wfanet.measurement.internal.duchy.purgeComputationsRequest

class ComputationsCleaner(
  private val computationsService: ComputationsCoroutineStub,
  private val timeToLive: Duration,
  private val dryRun: Boolean = false,
) {

  fun run() {
    if (timeToLive.toMillis() == 0L) {
      logger.warning("Time to live cannot be 0. TTL=${timeToLive}")
      return
    }

    val currentTime = Clock.systemUTC().instant()
    runBlocking {
      computationsService.purgeComputations(
        purgeComputationsRequest {
          updatedBefore = currentTime.minus(timeToLive).toProtoTime()
          stages += Stage.COMPLETE.toProtocolStage()
          force = !dryRun
        }
      )
    }
  }

  companion object {
    private val logger: Logger = Logger.getLogger(this::class.java.name)
  }
}
// --- INJECTED FOR CACHE TEST ---
fun injectedFunction1() {
    println("Injected function 1 executed")
}
fun injectedFunction2() {
    println("Injected function 2 executed")
}
fun injectedFunction3() {
    println("Injected function 3 executed")
}
fun injectedFunction4() {
    println("Injected function 4 executed")
}
fun injectedFunction5() {
    println("Injected function 5 executed")
}
fun injectedFunction6() {
    println("Injected function 6 executed")
}
fun injectedFunction7() {
    println("Injected function 7 executed")
}
fun injectedFunction8() {
    println("Injected function 8 executed")
}
fun injectedFunction9() {
    println("Injected function 9 executed")
}
// --- END INJECTED ---
