// Copyright 2021 The Cross-Media Measurement Authors
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

package org.wfanet.measurement.kingdom.service.internal.testing

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.ProtoTruth.assertThat
import com.google.protobuf.ByteString
import com.google.protobuf.timestamp
import com.google.rpc.errorInfo
import com.google.type.copy
import com.google.type.interval
import io.grpc.Status
import io.grpc.StatusRuntimeException
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.wfanet.measurement.common.grpc.errorInfo
import org.wfanet.measurement.common.identity.ExternalId
import org.wfanet.measurement.common.identity.IdGenerator
import org.wfanet.measurement.common.identity.InternalId
import org.wfanet.measurement.common.identity.RandomIdGenerator
import org.wfanet.measurement.common.toProtoTime
import org.wfanet.measurement.internal.kingdom.Certificate
import org.wfanet.measurement.internal.kingdom.DataProvider
import org.wfanet.measurement.internal.kingdom.DataProviderKt
import org.wfanet.measurement.internal.kingdom.DataProvidersGrpcKt.DataProvidersCoroutineImplBase
import org.wfanet.measurement.internal.kingdom.ErrorCode
import org.wfanet.measurement.internal.kingdom.GetDataProviderRequest
import org.wfanet.measurement.internal.kingdom.ModelLine
import org.wfanet.measurement.internal.kingdom.ModelLinesGrpcKt
import org.wfanet.measurement.internal.kingdom.ModelProvidersGrpcKt
import org.wfanet.measurement.internal.kingdom.ModelSuitesGrpcKt
import org.wfanet.measurement.internal.kingdom.batchGetDataProvidersRequest
import org.wfanet.measurement.internal.kingdom.certificate
import org.wfanet.measurement.internal.kingdom.certificateDetails
import org.wfanet.measurement.internal.kingdom.copy
import org.wfanet.measurement.internal.kingdom.dataProvider
import org.wfanet.measurement.internal.kingdom.dataProviderCapabilities
import org.wfanet.measurement.internal.kingdom.dataProviderDetails
import org.wfanet.measurement.internal.kingdom.getDataProviderRequest
import org.wfanet.measurement.internal.kingdom.modelLineKey
import org.wfanet.measurement.internal.kingdom.replaceDataAvailabilityIntervalRequest
import org.wfanet.measurement.internal.kingdom.replaceDataAvailabilityIntervalsRequest
import org.wfanet.measurement.internal.kingdom.replaceDataProviderCapabilitiesRequest
import org.wfanet.measurement.internal.kingdom.replaceDataProviderRequiredDuchiesRequest
import org.wfanet.measurement.kingdom.deploy.common.testing.DuchyIdSetter
import org.wfanet.measurement.kingdom.deploy.gcloud.spanner.common.KingdomInternalException
import org.wfanet.measurement.kingdom.service.internal.testing.Population.Companion.DUCHIES

@RunWith(JUnit4::class)
abstract class DataProvidersServiceTest<T : DataProvidersCoroutineImplBase> {
  @get:Rule val duchyIdSetter = DuchyIdSetter(DUCHIES)

  private val recordingIdGenerator = RecordingIdGenerator()
  protected val idGenerator: IdGenerator
    get() = recordingIdGenerator

  private val clock = Clock.systemUTC()
  private val population = Population(clock, idGenerator)

  protected lateinit var services: Services<T>
    private set

  protected val dataProvidersService: T
    get() = services.dataProvidersService

  protected abstract fun newServices(idGenerator: IdGenerator): Services<T>

  @Before
  fun initServices() {
    services = newServices(idGenerator)
    runBlocking {}
    runBlocking {}
    runBlocking {}

    runBlocking {}
    runBlocking {}
    runBlocking {}

    runBlocking {}
    runBlocking {}

    runBlocking {}

    runBlocking {}

    runBlocking {}


    runBlocking {}


    runBlocking {}

    runBlocking {}


    runBlocking {}

    runBlocking {}


    runBlocking {}


     runBlocking {}



    runBlocking {}
  }




  /** Random [IdGenerator] which records generated IDs. */
  private class RecordingIdGenerator : IdGenerator {
    private val delegate = RandomIdGenerator()
    private val mutableInternalIds: MutableList<InternalId> = mutableListOf()
    private val mutableExternalIds: MutableList<ExternalId> = mutableListOf()

    val internalIds: List<InternalId>
      get() = mutableInternalIds

    val externalIds: List<ExternalId>
      get() = mutableExternalIds

    override fun generateExternalId(): ExternalId {
      return delegate.generateExternalId().also { mutableExternalIds.add(it) }
    }

    override fun generateInternalId(): InternalId {
      return delegate.generateInternalId().also { mutableInternalIds.add(it) }
    }
  }

  protected data class Services<T>(
    val dataProvidersService: T,
  )

  companion object {
    private val CREATE_DATA_PROVIDER_REQUEST = dataProvider {}
  }
}
