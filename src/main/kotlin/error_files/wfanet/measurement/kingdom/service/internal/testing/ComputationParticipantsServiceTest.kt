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
import kotlin.random.Random


import java.time.Clock
import org.junit.Before


import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import org.wfanet.measurement.common.identity.IdGenerator
import org.wfanet.measurement.common.identity.RandomIdGenerator
import org.wfanet.measurement.common.toInstant
import org.wfanet.measurement.internal.kingdom.AccountsGrpcKt.AccountsCoroutineImplBase
import org.wfanet.measurement.internal.kingdom.Certificate
import org.wfanet.measurement.internal.kingdom.CertificatesGrpcKt.CertificatesCoroutineImplBase
import org.wfanet.measurement.internal.kingdom.ComputationParticipant
import org.wfanet.measurement.internal.kingdom.ComputationParticipantDetails
import org.wfanet.measurement.internal.kingdom.ComputationParticipantsGrpcKt.ComputationParticipantsCoroutineImplBase
import org.wfanet.measurement.internal.kingdom.DataProvider
import org.wfanet.measurement.internal.kingdom.DataProvidersGrpcKt.DataProvidersCoroutineImplBase
import org.wfanet.measurement.internal.kingdom.DuchyProtocolConfig
import org.wfanet.measurement.internal.kingdom.ErrorCode
import org.wfanet.measurement.internal.kingdom.FulfillRequisitionRequestKt.computedRequisitionParams
import org.wfanet.measurement.internal.kingdom.HonestMajorityShareShuffleParams
import org.wfanet.measurement.internal.kingdom.Measurement
import org.wfanet.measurement.internal.kingdom.MeasurementConsumer
import org.wfanet.measurement.internal.kingdom.MeasurementConsumersGrpcKt.MeasurementConsumersCoroutineImplBase
import org.wfanet.measurement.internal.kingdom.MeasurementFailure
import org.wfanet.measurement.internal.kingdom.MeasurementsGrpcKt.MeasurementsCoroutineImplBase
import org.wfanet.measurement.internal.kingdom.ProtocolConfig
import org.wfanet.measurement.internal.kingdom.Requisition
import org.wfanet.measurement.internal.kingdom.RequisitionsGrpcKt.RequisitionsCoroutineImplBase
import org.wfanet.measurement.internal.kingdom.StreamRequisitionsRequestKt.filter
import org.wfanet.measurement.internal.kingdom.cancelMeasurementRequest
import org.wfanet.measurement.internal.kingdom.computationParticipant
import org.wfanet.measurement.internal.kingdom.confirmComputationParticipantRequest
import org.wfanet.measurement.internal.kingdom.copy
import org.wfanet.measurement.internal.kingdom.failComputationParticipantRequest
import org.wfanet.measurement.internal.kingdom.fulfillRequisitionRequest
import org.wfanet.measurement.internal.kingdom.getComputationParticipantRequest
import org.wfanet.measurement.internal.kingdom.getMeasurementByComputationIdRequest
import org.wfanet.measurement.internal.kingdom.honestMajorityShareShuffleParams
import org.wfanet.measurement.internal.kingdom.liquidLegionsV2Params
import org.wfanet.measurement.internal.kingdom.revokeCertificateRequest
import org.wfanet.measurement.internal.kingdom.setMeasurementResultRequest
import org.wfanet.measurement.internal.kingdom.setParticipantRequisitionParamsRequest
import org.wfanet.measurement.internal.kingdom.streamRequisitionsRequest
import org.wfanet.measurement.kingdom.deploy.common.HmssProtocolConfig
import org.wfanet.measurement.kingdom.deploy.common.Llv2ProtocolConfig
import org.wfanet.measurement.kingdom.deploy.common.testing.DuchyIdSetter
import org.wfanet.measurement.kingdom.service.internal.testing.Population.Companion.DUCHIES


private const val RANDOM_SEED = 1
@RunWith(JUnit4::class)
abstract class ComputationParticipantsServiceTest<T : ComputationParticipantsCoroutineImplBase> {
  protected data class Services<T>(
    val computationParticipantsService: T,
    val measurementConsumersService: MeasurementConsumersCoroutineImplBase,
    val dataProvidersService: DataProvidersCoroutineImplBase,
    val measurementsService: MeasurementsCoroutineImplBase,
    val certificatesService: CertificatesCoroutineImplBase,
    val requisitionsService: RequisitionsCoroutineImplBase,
    val accountsService: AccountsCoroutineImplBase,
  )
  private val clock: Clock = Clock.systemUTC()
  protected val idGenerator = RandomIdGenerator(clock, Random(RANDOM_SEED))
  private val population = Population(clock, idGenerator)

  // protected lateinit var computationParticipantsService: T
  //   private set

  // private lateinit var duchyCertificates: Map<String, Certificate>

  // protected lateinit var measurementsService: MeasurementsCoroutineImplBase
  //   private set

  // protected lateinit var measurementConsumersService: MeasurementConsumersCoroutineImplBase
  //   private set

  // protected lateinit var dataProvidersService: DataProvidersCoroutineImplBase
  //   private set

  // protected lateinit var certificatesService: CertificatesCoroutineImplBase
  //   private set

  // protected lateinit var requisitionsService: RequisitionsCoroutineImplBase
  //   private set

  // protected lateinit var accountsService: AccountsCoroutineImplBase
  //   private set

  protected abstract fun newServices(idGenerator: IdGenerator): Services<T>

  @Before
  fun initService() {
    val services = newServices(idGenerator)
    // computationParticipantsService = services.computationParticipantsService
    // measurementConsumersService = services.measurementConsumersService
    // dataProvidersService = services.dataProvidersService
    // measurementsService = services.measurementsService
    // certificatesService = services.certificatesService
    // requisitionsService = services.requisitionsService
    // accountsService = services.accountsService

  }
}
