// Copyright 2025 The Cross-Media Measurement Authors
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

package org.wfanet.measurement.integration.deploy.gcloud

import java.nio.file.Files
import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.Timeout
import org.wfanet.measurement.gcloud.pubsub.testing.GooglePubSubEmulatorClient
import org.wfanet.measurement.gcloud.spanner.testing.SpannerEmulatorRule
import org.wfanet.measurement.integration.common.ALL_DUCHY_NAMES
import org.wfanet.measurement.integration.common.InProcessCmmsComponents
import org.wfanet.measurement.integration.common.InProcessEdpAggregatorComponents
import org.wfanet.measurement.integration.common.InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest

/**
 * Implementation of [InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest] for GCloud backends
 * with Spanner database.
 */
class GCloudEdpAggregatorLifeOfAMeasurementIntegrationTest :
  InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest(
    inProcessCmmsComponents = cmmsComponents,
    inProcessEdpAggregatorComponents = edpAggregatorComponents,
    pubSubClient = pubSubClient,
  ) {

  /**
   * Rule to enforce test method timeout.
   *
   * TODO(Kotlin/kotlinx.coroutines#3865): Switch back to CoroutinesTimeout when fixed.
   */
  @get:Rule val timeout: Timeout = Timeout.seconds(180)

  companion object {
    @get:ClassRule @JvmStatic val spannerEmulator = SpannerEmulatorRule()
    @JvmStatic
    private val pubSubClient =
      GooglePubSubEmulatorClient(
        host = InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest.pubSubEmulatorProvider.host,
        port = InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest.pubSubEmulatorProvider.port,
      )

    @JvmStatic private val storagePath = Files.createTempDirectory("edp-agg-")

    @JvmStatic
    private val syntheticEventGroupMap =
      mapOf(
        "edpa-eg-reference-id-1" to
          InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest.syntheticEventGroupSpec,
        "edpa-eg-reference-id-2" to
          InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest.syntheticEventGroupSpec,
      )

    @get:ClassRule
    @JvmStatic
    val cmmsComponents =
      InProcessCmmsComponents(
        kingdomDataServicesRule = KingdomDataServicesProviderRule(spannerEmulator),
        duchyDependenciesRule = SpannerDuchyDependencyProviderRule(spannerEmulator, ALL_DUCHY_NAMES),
        useEdpSimulators = false,
      )

    @get:ClassRule
    @JvmStatic
    val edpAggregatorComponents =
      InProcessEdpAggregatorComponents(
        secureComputationDatabaseAdmin = spannerEmulator,
        storagePath = storagePath,
        pubSubClient = pubSubClient,
        syntheticEventGroupMap = syntheticEventGroupMap,
        syntheticPopulationSpec =
          InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest.syntheticPopulationSpec,
        modelLineInfoMap = InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest.modelLineInfoMap,
      )
  }
}
