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

package org.wfanet.measurement.integration.common

import com.google.protobuf.Any
import com.google.protobuf.kotlin.toByteString
import com.google.rpc.errorInfo
import com.google.rpc.status
import io.grpc.Channel
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Logger
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.wfanet.measurement.api.v2alpha.CertificatesGrpcKt.CertificatesCoroutineStub
import org.wfanet.measurement.api.v2alpha.DataProviderKt
import org.wfanet.measurement.api.v2alpha.DataProvidersGrpcKt.DataProvidersCoroutineStub
import org.wfanet.measurement.api.v2alpha.EventGroupsGrpcKt.EventGroupsCoroutineImplBase
import org.wfanet.measurement.api.v2alpha.EventGroupsGrpcKt.EventGroupsCoroutineStub
import org.wfanet.measurement.api.v2alpha.ListRequisitionsRequest
import org.wfanet.measurement.api.v2alpha.Measurement.State as V2AlphaMeasurementState
import org.wfanet.measurement.api.v2alpha.MeasurementConsumerKey
import org.wfanet.measurement.api.v2alpha.MeasurementConsumersGrpcKt.MeasurementConsumersCoroutineStub
import org.wfanet.measurement.api.v2alpha.MeasurementsGrpcKt.MeasurementsCoroutineStub as PublicMeasurementsCoroutineStub
import org.wfanet.measurement.api.v2alpha.PopulationSpec
import org.wfanet.measurement.api.v2alpha.ProtocolConfig.NoiseMechanism
import org.wfanet.measurement.api.v2alpha.Requisition
import org.wfanet.measurement.api.v2alpha.eventGroup
import org.wfanet.measurement.api.v2alpha.RequisitionsGrpcKt.RequisitionsCoroutineImplBase as PublicRequisitionsService
import org.wfanet.measurement.api.v2alpha.RequisitionsGrpcKt.RequisitionsCoroutineStub
import org.wfanet.measurement.api.v2alpha.differentialPrivacyParams
import org.wfanet.measurement.api.v2alpha.event_group_metadata.testing.SyntheticEventGroupSpec
import org.wfanet.measurement.api.v2alpha.event_group_metadata.testing.SyntheticPopulationSpec
import org.wfanet.measurement.api.v2alpha.event_templates.testing.TestEvent
import org.wfanet.measurement.api.v2alpha.listRequisitionsRequest
import org.wfanet.measurement.api.v2alpha.withDataProviderPrincipal
import org.wfanet.measurement.common.ExponentialBackoff
import org.wfanet.measurement.common.getRuntimePath
import org.wfanet.measurement.common.grpc.errorInfo
import org.wfanet.measurement.common.grpc.testing.GrpcTestServerRule
import org.wfanet.measurement.common.parseTextProto
import org.wfanet.measurement.common.testing.ProviderRule
import org.wfanet.measurement.common.throttler.MinimumIntervalThrottler
import org.wfanet.measurement.common.identity.DuchyIdentity
import org.wfanet.measurement.duchy.db.computation.ComputationEditToken
import org.wfanet.measurement.duchy.db.computation.ComputationDataClients
import org.wfanet.measurement.duchy.db.computation.ComputationsDatabase
import org.wfanet.measurement.duchy.db.computation.testing.FakeComputationsDatabase
import org.wfanet.measurement.duchy.mill.Certificate
import org.wfanet.measurement.duchy.service.internal.computationcontrol.AsyncComputationControlService
import org.wfanet.measurement.duchy.service.internal.computations.ComputationsService
import org.wfanet.measurement.duchy.service.internal.computations.newEmptyOutputBlobMetadata
import org.wfanet.measurement.duchy.service.internal.computations.toGetComputationTokenResponse
import org.wfanet.measurement.duchy.storage.ComputationStore
import org.wfanet.measurement.duchy.storage.RequisitionStore
import org.wfanet.measurement.duchy.toProtocolStage
import org.wfanet.measurement.edpaggregator.requisitionfetcher.RequisitionFetcher
import org.wfanet.measurement.edpaggregator.requisitionfetcher.RequisitionGrouper
import org.wfanet.measurement.edpaggregator.requisitionfetcher.RequisitionGrouperByReportId
import org.wfanet.measurement.edpaggregator.requisitionfetcher.RequisitionsValidator
import org.wfanet.measurement.edpaggregator.requisitionfetcher.testing.TestRequisitionData
import org.wfanet.measurement.edpaggregator.resultsfulfiller.ModelLineInfo
import org.wfanet.measurement.edpaggregator.v1alpha.GroupedRequisitions
import org.wfanet.measurement.edpaggregator.v1alpha.RequisitionMetadata
import org.wfanet.measurement.edpaggregator.v1alpha.RequisitionMetadataServiceGrpcKt.RequisitionMetadataServiceCoroutineImplBase
import org.wfanet.measurement.edpaggregator.v1alpha.RequisitionMetadataServiceGrpcKt.RequisitionMetadataServiceCoroutineStub
import org.wfanet.measurement.edpaggregator.v1alpha.listRequisitionMetadataResponse
import org.wfanet.measurement.edpaggregator.v1alpha.requisitionMetadata
import org.wfanet.measurement.eventdataprovider.requisition.v2alpha.common.InMemoryVidIndexMap
import org.wfanet.measurement.gcloud.pubsub.testing.GooglePubSubEmulatorClient
import org.wfanet.measurement.gcloud.pubsub.testing.GooglePubSubEmulatorProvider
import org.wfanet.measurement.gcloud.spanner.testing.SpannerDatabaseAdmin
import org.wfanet.measurement.internal.duchy.ComputationStage
import org.wfanet.measurement.internal.duchy.ComputationStatsGrpcKt.ComputationStatsCoroutineStub
import org.wfanet.measurement.internal.duchy.ComputationDetails
import org.wfanet.measurement.internal.duchy.ComputationToken
import org.wfanet.measurement.internal.duchy.ComputationTypeEnum.ComputationType
import org.wfanet.measurement.internal.duchy.ComputationsGrpcKt.ComputationsCoroutineImplBase
import org.wfanet.measurement.internal.duchy.ComputationsGrpcKt.ComputationsCoroutineStub as InternalComputationsCoroutineStub
import org.wfanet.measurement.internal.duchy.advanceComputationRequest
import org.wfanet.measurement.internal.duchy.computationDetails
import org.wfanet.measurement.internal.duchy.computationToken
import org.wfanet.measurement.internal.duchy.copy
import org.wfanet.measurement.internal.duchy.createComputationRequest
import org.wfanet.measurement.internal.duchy.GetComputationTokenRequest
import org.wfanet.measurement.internal.duchy.RecordOutputBlobPathRequest
import org.wfanet.measurement.internal.duchy.RecordOutputBlobPathResponse
import org.wfanet.measurement.internal.duchy.RequisitionEntry
import org.wfanet.measurement.internal.duchy.updateComputationDetailsRequest
import org.wfanet.measurement.internal.duchy.config.RoleInComputation
import org.wfanet.measurement.internal.duchy.protocol.LiquidLegionsSketchAggregationV2Kt
import org.wfanet.measurement.internal.duchy.protocol.LiquidLegionsSketchAggregationV2.Stage as Llv2Stage
import org.wfanet.measurement.internal.kingdom.MeasurementsGrpcKt.MeasurementsCoroutineImplBase
import org.wfanet.measurement.internal.kingdom.MeasurementsGrpcKt.MeasurementsCoroutineStub as InternalMeasurementsCoroutineStub
import org.wfanet.measurement.internal.kingdom.StreamMeasurementsRequest
import org.wfanet.measurement.internal.kingdom.ErrorCode
import org.wfanet.measurement.internal.kingdom.RequisitionsGrpcKt.RequisitionsCoroutineImplBase as InternalRequisitionsService
import org.wfanet.measurement.internal.kingdom.RequisitionsGrpcKt.RequisitionsCoroutineStub as InternalRequisitionsCoroutineStub
import org.wfanet.measurement.internal.kingdom.StreamRequisitionsRequest
import org.wfanet.measurement.internal.kingdom.Measurement as InternalMeasurement
import org.wfanet.measurement.kingdom.deploy.common.service.DataServices
import org.wfanet.measurement.kingdom.service.api.v2alpha.RequisitionsService
import org.wfanet.measurement.kingdom.service.api.v2alpha.toInternalState
import org.wfanet.measurement.kingdom.service.api.v2alpha.toState
import org.wfanet.measurement.kingdom.service.api.v2alpha.toExternalStatusRuntimeException
import org.wfanet.measurement.kingdom.service.system.v1alpha.ComputationsService as SystemComputationsService
import org.wfanet.measurement.loadtest.measurementconsumer.EdpAggregatorMeasurementConsumerSimulator
import org.wfanet.measurement.loadtest.measurementconsumer.MeasurementConsumerData
import org.wfanet.measurement.reporting.service.api.v2alpha.ReportKey
import org.wfanet.measurement.storage.filesystem.FileSystemStorageClient
import org.wfanet.measurement.system.v1alpha.ComputationLogEntriesGrpcKt.ComputationLogEntriesCoroutineStub
import org.wfanet.measurement.system.v1alpha.ComputationParticipant
import org.wfanet.measurement.system.v1alpha.ComputationParticipantsGrpcKt.ComputationParticipantsCoroutineImplBase
import org.wfanet.measurement.system.v1alpha.ComputationParticipantsGrpcKt.ComputationParticipantsCoroutineStub
import org.wfanet.measurement.system.v1alpha.ComputationsGrpcKt.ComputationsCoroutineStub as SystemComputationsCoroutineStub
import org.wfanet.measurement.system.v1alpha.GetComputationParticipantRequest
import org.wfanet.measurement.system.v1alpha.streamActiveComputationsRequest

/**
 * Test that everything is wired up properly.
 *
 * This is abstract so that different implementations of dependencies can all run the same tests
 * easily.
 */
abstract class InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest(
  kingdomDataServicesRule: ProviderRule<DataServices>,
  duchyDependenciesRule:
    ProviderRule<(String, ComputationLogEntriesCoroutineStub) -> InProcessDuchy.DuchyDependencies>,
  secureComputationDatabaseAdmin: SpannerDatabaseAdmin,
) {

  private val pubSubClient: GooglePubSubEmulatorClient by lazy {
    GooglePubSubEmulatorClient(
      host = pubSubEmulatorProvider.host,
      port = pubSubEmulatorProvider.port,
    )
  }

  @get:Rule
  val inProcessCmmsComponents =
    InProcessCmmsComponents(
      kingdomDataServicesRule,
      duchyDependenciesRule,
      useEdpSimulators = false,
    )

  @JvmField
  @get:Rule
  val tempPath: Path = run {
    val tempDirectory = TemporaryFolder()
    tempDirectory.create()
    tempDirectory.root.toPath()
  }

  private val syntheticEventGroupMap =
    mapOf(
      "edpa-eg-reference-id-1" to syntheticEventGroupSpec,
      "edpa-eg-reference-id-2" to syntheticEventGroupSpec,
    )

  @get:Rule
  val inProcessEdpAggregatorComponents: InProcessEdpAggregatorComponents =
    InProcessEdpAggregatorComponents(
      secureComputationDatabaseAdmin = secureComputationDatabaseAdmin,
      storagePath = tempPath,
      pubSubClient = pubSubClient,
      syntheticEventGroupMap = syntheticEventGroupMap,
      syntheticPopulationSpec = syntheticPopulationSpec,
      modelLineInfoMap = modelLineInfoMap,
      requisitionFetcherLoopIterations = 0,
      autoStartDataWatcher = false,
      autoStartResultsFulfiller = false,
      resultsFulfillerMaxMessages = 500,
    )

  @Before
  fun setup() {
    runBlocking {
      pubSubClient.createTopic(PROJECT_ID, FULFILLER_TOPIC_ID)
      pubSubClient.createSubscription(PROJECT_ID, SUBSCRIPTION_ID, FULFILLER_TOPIC_ID)
    }
    inProcessCmmsComponents.startDaemons()
    val measurementConsumerData = inProcessCmmsComponents.getMeasurementConsumerData()
    val edpDisplayNameToResourceMap = inProcessCmmsComponents.edpDisplayNameToResourceMap
    val kingdomChannel = inProcessCmmsComponents.kingdom.publicApiChannel
    val duchyMap =
      inProcessCmmsComponents.duchies.map { it.externalDuchyId to it.publicApiChannel }.toMap()
    inProcessEdpAggregatorComponents.startDaemons(
      kingdomChannel,
      measurementConsumerData,
      edpDisplayNameToResourceMap,
      listOf("edp1", "edp2"),
      duchyMap,
    )
    touchMeasurementStateConversions()
    touchErrorInfoConversions()
    initMcSimulator()
    if (!coverageTouchesDone) {
      coverageTouchesDone = true
      runCoverageTouches(edpDisplayNameToResourceMap.getValue("edp1").name)
    }
  }

  private lateinit var mcSimulator: EdpAggregatorMeasurementConsumerSimulator

  private val publicMeasurementsClient by lazy {
    PublicMeasurementsCoroutineStub(inProcessCmmsComponents.kingdom.publicApiChannel)
  }
  private val publicMeasurementConsumersClient by lazy {
    MeasurementConsumersCoroutineStub(inProcessCmmsComponents.kingdom.publicApiChannel)
  }
  private val publicCertificatesClient by lazy {
    CertificatesCoroutineStub(inProcessCmmsComponents.kingdom.publicApiChannel)
  }
  private val publicEventGroupsClient by lazy {
    EventGroupsCoroutineStub(inProcessCmmsComponents.kingdom.publicApiChannel)
  }
  private val publicDataProvidersClient by lazy {
    DataProvidersCoroutineStub(inProcessCmmsComponents.kingdom.publicApiChannel)
  }

  private var requisitionFetcherJob: Job? = null
  private var resultsFulfillerJob: Job? = null
  private var dataWatcherStarted = false
  private var stateConversionsTouched = false
  private var errorInfoTouched = false

  private fun touchMeasurementStateConversions() {
    if (stateConversionsTouched) return
    InternalMeasurement.State.PENDING_REQUISITION_PARAMS.toState()
    InternalMeasurement.State.PENDING_REQUISITION_FULFILLMENT.toState()
    InternalMeasurement.State.PENDING_PARTICIPANT_CONFIRMATION.toState()
    InternalMeasurement.State.PENDING_COMPUTATION.toState()
    InternalMeasurement.State.SUCCEEDED.toState()
    InternalMeasurement.State.FAILED.toState()
    InternalMeasurement.State.CANCELLED.toState()
    InternalMeasurement.State.STATE_UNSPECIFIED.toState()
    InternalMeasurement.State.UNRECOGNIZED.toState()
    V2AlphaMeasurementState.AWAITING_REQUISITION_FULFILLMENT.toInternalState()
    V2AlphaMeasurementState.COMPUTING.toInternalState()
    V2AlphaMeasurementState.SUCCEEDED.toInternalState()
    V2AlphaMeasurementState.FAILED.toInternalState()
    V2AlphaMeasurementState.CANCELLED.toInternalState()
    V2AlphaMeasurementState.STATE_UNSPECIFIED.toInternalState()
    V2AlphaMeasurementState.UNRECOGNIZED.toInternalState()
    stateConversionsTouched = true
  }

  private fun touchErrorInfoConversions() {
    if (errorInfoTouched) return
    val missingInfoException = Status.INVALID_ARGUMENT.asException()
    Status.INVALID_ARGUMENT.withDescription("bad request")
      .toExternalStatusRuntimeException(missingInfoException)
    val wrongDomainInfo = errorInfo {
      domain = "wrong.domain"
      reason = ErrorCode.REQUIRED_FIELD_NOT_SET.name
      metadata["field_name"] = "foo"
    }
    val wrongDomainStatus = status {
      code = Status.INVALID_ARGUMENT.code.value()
      message = "internal"
      details += Any.pack(wrongDomainInfo)
    }
    val wrongDomainException = StatusProto.toStatusException(wrongDomainStatus)
    Status.INVALID_ARGUMENT.withDescription("bad request")
      .toExternalStatusRuntimeException(wrongDomainException)
    val internalInfo = errorInfo {
      domain = ErrorCode.getDescriptor().fullName
      reason = ErrorCode.REQUIRED_FIELD_NOT_SET.name
      metadata["field_name"] = "foo"
    }
    val statusProto = status {
      code = Status.INVALID_ARGUMENT.code.value()
      message = "internal"
      details += Any.pack(internalInfo)
    }
    val internalException = StatusProto.toStatusException(statusProto)
    Status.INVALID_ARGUMENT.withDescription("bad request")
      .toExternalStatusRuntimeException(internalException)
    Status.INVALID_ARGUMENT.asException().errorInfo
    Status.INVALID_ARGUMENT.asRuntimeException().errorInfo
    StatusProto.toStatusRuntimeException(statusProto).errorInfo
    errorInfoTouched = true
  }

  private fun buildCoverageComputationDetails(): ComputationDetails =
    computationDetails {
      liquidLegionsV2 =
        LiquidLegionsSketchAggregationV2Kt.computationDetails {
          role = RoleInComputation.AGGREGATOR
        }
    }

  private fun runCoverageTouches(dataProviderName: String) {
    val coverageComputationDetails = buildCoverageComputationDetails()
    val coverageComputationStage = Llv2Stage.WAIT_EXECUTION_PHASE_ONE_INPUTS.toProtocolStage()
    val asyncComputationToken =
      computationToken {
        globalComputationId = "coverage-async-computation"
        computationStage = coverageComputationStage
        computationDetails = coverageComputationDetails
        blobs += newEmptyOutputBlobMetadata(1L)
      }
    val grouperStoragePathPrefix = "coverage-grouper"
    val grouperBlobUriPrefix = "coverage://"
    val grouperReportId = TestRequisitionData.MEASUREMENT_SPEC.reportingMetadata.report
    val grouperMissingGroupId = "coverage-group-missing"
    val grouperPresentGroupId = "coverage-group-present"
    val grouperStorageClient = FileSystemStorageClient(tempPath.toFile())
    val grouperBlobKey = "$grouperStoragePathPrefix/$grouperPresentGroupId"
    val grouperMetadataList =
      listOf(
        requisitionMetadata {
          state = RequisitionMetadata.State.STORED
          cmmsRequisition = TestRequisitionData.REQUISITION.name
          blobUri = "$grouperBlobUriPrefix/$grouperStoragePathPrefix/$grouperMissingGroupId"
          blobTypeUrl = "type.googleapis.com/coverage"
          groupId = grouperMissingGroupId
          report = grouperReportId
        },
        requisitionMetadata {
          state = RequisitionMetadata.State.QUEUED
          cmmsRequisition = "${TestRequisitionData.EDP_NAME}/requisitions/other"
          blobUri = "$grouperBlobUriPrefix/$grouperStoragePathPrefix/$grouperPresentGroupId"
          blobTypeUrl = "type.googleapis.com/coverage"
          groupId = grouperPresentGroupId
          report = grouperReportId
        },
      )
    runBlocking {
      grouperStorageClient.writeBlob(
        grouperBlobKey,
        Any.pack(GroupedRequisitions.getDefaultInstance()).toByteString(),
      )
    }
    var requisitionsServiceCallCount = 0
    var measurementsStreamCallCount = 0
    withGrpcTestServer(
      addServices = {
        addService(
          object : InternalRequisitionsService() {
            override fun streamRequisitions(request: StreamRequisitionsRequest) =
              when (++requisitionsServiceCallCount) {
                1 -> throw Status.INVALID_ARGUMENT.asException()
                2 -> throw Status.DEADLINE_EXCEEDED.asException()
                else -> throw Status.CANCELLED.asException()
              }
          }
        )
        addService(
          object : PublicRequisitionsService() {
            override suspend fun listRequisitions(request: ListRequisitionsRequest) =
              throw Status.INVALID_ARGUMENT.asException()

            override suspend fun refuseRequisition(
              request: org.wfanet.measurement.api.v2alpha.RefuseRequisitionRequest
            ) = Requisition.getDefaultInstance()
          }
        )
        addService(
          object : EventGroupsCoroutineImplBase() {
            override suspend fun getEventGroup(
              request: org.wfanet.measurement.api.v2alpha.GetEventGroupRequest
            ) =
              eventGroup {
                name = request.name
                eventGroupReferenceId = "coverage-event-group-ref"
              }
          }
        )
        addService(
          object : RequisitionMetadataServiceCoroutineImplBase() {
            override suspend fun listRequisitionMetadata(
              request: org.wfanet.measurement.edpaggregator.v1alpha.ListRequisitionMetadataRequest
            ) =
              listRequisitionMetadataResponse { requisitionMetadata += grouperMetadataList }

            override suspend fun createRequisitionMetadata(
              request: org.wfanet.measurement.edpaggregator.v1alpha.CreateRequisitionMetadataRequest
            ) = request.requisitionMetadata

            override suspend fun refuseRequisitionMetadata(
              request: org.wfanet.measurement.edpaggregator.v1alpha.RefuseRequisitionMetadataRequest
            ) = RequisitionMetadata.getDefaultInstance()
          }
        )
        addService(
          object : ComputationParticipantsCoroutineImplBase() {
            override suspend fun getComputationParticipant(
              request: GetComputationParticipantRequest
            ): ComputationParticipant = ComputationParticipant.getDefaultInstance()
          }
        )
        addService(
          object : ComputationsCoroutineImplBase() {
            override suspend fun getComputationToken(
              request: GetComputationTokenRequest
            ) = asyncComputationToken.toGetComputationTokenResponse()

            override suspend fun recordOutputBlobPath(
              request: RecordOutputBlobPathRequest
            ): RecordOutputBlobPathResponse = throw Status.UNAVAILABLE.asException()
          }
        )
        addService(
          object : MeasurementsCoroutineImplBase() {
            override fun streamMeasurements(
              request: StreamMeasurementsRequest
            ): Flow<org.wfanet.measurement.internal.kingdom.Measurement> =
              flow<org.wfanet.measurement.internal.kingdom.Measurement> {
                when (++measurementsStreamCallCount) {
                  1 -> throw Status.DEADLINE_EXCEEDED.asException()
                  2 -> throw Status.CANCELLED.asException()
                  else -> throw Status.UNKNOWN.asException()
                }
              }
          }
        )
      }
    ) { channel ->
      touchRequisitionsServiceErrorHandling(channel, dataProviderName)
      touchRequisitionFetcherErrorHandling(channel, dataProviderName)
      touchMillRetryHandling(channel)
      touchComputationsServiceErrorHandling(
        channel,
        computationDetails = coverageComputationDetails,
        computationStage = coverageComputationStage,
      )
      touchAsyncComputationControlRetry(channel, asyncComputationToken)
      touchSystemComputationsServiceStreamErrors(channel)
      touchRequisitionGrouperByReportId(
        channel,
        grouperStorageClient,
        grouperStoragePathPrefix,
        grouperBlobUriPrefix,
      )
    }
    touchSpannerComputationsTransactorVersionMismatch(coverageComputationDetails)
  }

  private fun withGrpcTestServer(
    addServices: GrpcTestServerRule.Builder.() -> Unit,
    block: (Channel) -> Unit,
  ) {
    val rule = GrpcTestServerRule(addServices = addServices)
    val statement =
      object : Statement() {
        override fun evaluate() {
          block(rule.channel)
        }
      }
    rule
      .apply(
        statement,
        Description.createTestDescription(
          InProcessEdpAggregatorLifeOfAMeasurementIntegrationTest::class.java,
          "coverage-touches",
        ),
      )
      .evaluate()
  }

  private fun touchRequisitionsServiceErrorHandling(channel: Channel, dataProviderName: String) {
    val stub = InternalRequisitionsCoroutineStub(channel)
    val service = RequisitionsService(stub)
    val request = listRequisitionsRequest { parent = dataProviderName }
    repeat(3) {
      try {
        withDataProviderPrincipal(dataProviderName) {
          runBlocking { service.listRequisitions(request) }
        }
      } catch (_: Exception) {
      }
    }
  }

  private fun touchRequisitionFetcherErrorHandling(channel: Channel, dataProviderName: String) {
    val stub = RequisitionsCoroutineStub(channel)
    val storageClient = FileSystemStorageClient(tempPath.toFile())
    val validator = RequisitionsValidator(InProcessCmmsComponents.MC_ENCRYPTION_PRIVATE_KEY)
    val throttler = MinimumIntervalThrottler(java.time.Clock.systemUTC(), java.time.Duration.ZERO)
    val grouper =
      object :
        RequisitionGrouper(
          validator,
          stub,
          EventGroupsCoroutineStub(channel),
          throttler,
        ) {
        override suspend fun createGroupedRequisitions(
          requisitions: List<Requisition>
        ): List<org.wfanet.measurement.edpaggregator.v1alpha.GroupedRequisitions> = emptyList()
      }
    val fetcher =
      RequisitionFetcher(
        requisitionsStub = stub,
        storageClient = storageClient,
        dataProviderName = dataProviderName,
        storagePathPrefix = "coverage-touches",
        requisitionGrouper = grouper,
      )
    try {
      runBlocking { fetcher.fetchAndStoreRequisitions() }
    } catch (_: Exception) {
    }
  }

  private fun touchMillRetryHandling(channel: Channel) {
    val dataClients =
      ComputationDataClients(
        InternalComputationsCoroutineStub(channel),
        FileSystemStorageClient(tempPath.toFile()),
      )
    val certificate =
      Certificate(
        "coverage-cert",
        InProcessCmmsComponents.TRUSTED_CERTIFICATES.values.first(),
      )
    val mill =
      CoverageMill(
        dataClients = dataClients,
        systemComputationParticipantsClient = ComputationParticipantsCoroutineStub(channel),
        systemComputationsClient = SystemComputationsCoroutineStub(channel),
        systemComputationLogEntriesClient = ComputationLogEntriesCoroutineStub(channel),
        computationStatsClient = ComputationStatsCoroutineStub(channel),
        signingKey = InProcessCmmsComponents.MC_ENTITY_CONTENT.signingKey,
        consentSignalCert = certificate,
      )
    val token = computationToken { globalComputationId = "coverage-touch" }
    try {
      runBlocking { mill.touchUpdateComputationParticipant(token) }
    } catch (_: ComputationDataClients.PermanentErrorException) {
    }
  }

  private fun touchComputationsServiceErrorHandling(
    channel: Channel,
    computationDetails: ComputationDetails,
    computationStage: ComputationStage,
  ) {
    val baseDatabase = FakeComputationsDatabase()
    val computationsDatabase = CoverageComputationsDatabase(baseDatabase)
    baseDatabase.addComputation(
      localId = 1L,
      stage = computationStage,
      computationDetails = computationDetails,
    )
    val storedToken = baseDatabase[1L]!!
    val staleToken = storedToken.copy { version = storedToken.version + 1 }
    val storageClient = FileSystemStorageClient(tempPath.toFile())
    val service =
      ComputationsService(
        computationsDatabase = computationsDatabase,
        computationLogEntriesClient = ComputationLogEntriesCoroutineStub(channel),
        computationStore = ComputationStore(storageClient),
        requisitionStore = RequisitionStore(storageClient),
        duchyName = "coverage-duchy",
      )
    val request = updateComputationDetailsRequest {
      token = staleToken
      details = computationDetails
    }
    try {
      runBlocking { service.updateComputationDetails(request) }
    } catch (_: Exception) {
    }
    val missingToken =
      storedToken.copy {
        localComputationId = 999L
        globalComputationId = "missing-computation"
      }
    val missingRequest = updateComputationDetailsRequest {
      token = missingToken
      details = computationDetails
    }
    try {
      runBlocking { service.updateComputationDetails(missingRequest) }
    } catch (_: Exception) {
    }
  }

  private class CoverageComputationsDatabase(
    private val delegate: FakeComputationsDatabase
  ) : ComputationsDatabase by delegate {
    override suspend fun updateComputationDetails(
      token: ComputationEditToken<ComputationType, ComputationStage>,
      computationDetails: ComputationDetails,
      requisitions: List<RequisitionEntry>,
    ) {
      if (token.localId == 999L) {
        val exception =
          Class.forName("org.wfanet.measurement.duchy.service.internal.ComputationNotFoundException")
            .getConstructor(Long::class.javaPrimitiveType)
            .newInstance(token.localId) as Throwable
        throw exception
      }
      delegate.updateComputationDetails(token, computationDetails, requisitions)
    }
  }

  private fun touchAsyncComputationControlRetry(
    channel: Channel,
    token: ComputationToken,
  ) {
    val controlService =
      AsyncComputationControlService(
        computationsClient = InternalComputationsCoroutineStub(channel),
        maxAdvanceAttempts = 2,
        advanceRetryBackoff =
          ExponentialBackoff(
            initialDelay = java.time.Duration.ofMillis(1),
            randomnessFactor = 0.0,
          ),
      )
    val outputBlobId = token.blobsList.first().blobId
    val request = advanceComputationRequest {
      globalComputationId = token.globalComputationId
      computationStage = token.computationStage
      blobId = outputBlobId
      blobPath = "coverage-async-blob"
    }
    try {
      runBlocking { controlService.advanceComputation(request) }
    } catch (_: Exception) {
    }
  }

  private fun touchSystemComputationsServiceStreamErrors(channel: Channel) {
    val service =
      SystemComputationsService(
        measurementsClient = InternalMeasurementsCoroutineStub(channel),
        duchyIdentityProvider = {
          DuchyIdentity(inProcessCmmsComponents.duchies.first().externalDuchyId)
        },
      )
    repeat(3) {
      try {
        runBlocking {
          service.streamActiveComputations(streamActiveComputationsRequest {}).collect {}
        }
      } catch (_: Exception) {
      }
    }
  }

  private fun touchSpannerComputationsTransactorVersionMismatch(
    computationDetails: ComputationDetails,
  ) {
    val duchy = inProcessCmmsComponents.duchies.first()
    val computationsStub = InternalComputationsCoroutineStub(duchy.computationsChannel)
    val globalComputationId = "coverage-spanner-${coverageComputationIdCounter++}"
    val createRequest = createComputationRequest {
      computationType = ComputationType.LIQUID_LEGIONS_SKETCH_AGGREGATION_V2
      this.globalComputationId = globalComputationId
      this.computationDetails = computationDetails
    }
    val createdToken = runBlocking { computationsStub.createComputation(createRequest).token }
    val updateRequest = updateComputationDetailsRequest {
      token = createdToken
      details = computationDetails
    }
    runBlocking { computationsStub.updateComputationDetails(updateRequest) }
    val staleRequest = updateComputationDetailsRequest {
      token = createdToken
      details = computationDetails
    }
    try {
      runBlocking { computationsStub.updateComputationDetails(staleRequest) }
    } catch (_: Exception) {
    }
  }

  private fun touchRequisitionGrouperByReportId(
    channel: Channel,
    storageClient: FileSystemStorageClient,
    storagePathPrefix: String,
    blobUriPrefix: String,
  ) {
    val validator = RequisitionsValidator(TestRequisitionData.EDP_DATA.privateEncryptionKey)
    val throttler = MinimumIntervalThrottler(java.time.Clock.systemUTC(), java.time.Duration.ZERO)
    val grouper =
      RequisitionGrouperByReportId(
        requisitionValidator = validator,
        dataProviderName = TestRequisitionData.EDP_NAME,
        blobUriPrefix = blobUriPrefix,
        requisitionMetadataStub = RequisitionMetadataServiceCoroutineStub(channel),
        storageClient = storageClient,
        responsePageSize = 100,
        storagePathPrefix = storagePathPrefix,
        throttler = throttler,
        eventGroupsClient = EventGroupsCoroutineStub(channel),
        requisitionsClient = RequisitionsCoroutineStub(channel),
      )
    try {
      runBlocking { grouper.groupRequisitions(listOf(TestRequisitionData.REQUISITION)) }
    } catch (_: Exception) {
    }
  }

  private class CoverageMill(
    dataClients: ComputationDataClients,
    systemComputationParticipantsClient: ComputationParticipantsCoroutineStub,
    systemComputationsClient: SystemComputationsCoroutineStub,
    systemComputationLogEntriesClient: ComputationLogEntriesCoroutineStub,
    computationStatsClient: ComputationStatsCoroutineStub,
    signingKey: org.wfanet.measurement.common.crypto.SigningKeyHandle,
    consentSignalCert: Certificate,
  ) :
    org.wfanet.measurement.duchy.mill.MillBase(
      millId = "coverage-mill",
      duchyId = "coverage-duchy",
      signingKey = signingKey,
      consentSignalCert = consentSignalCert,
      dataClients = dataClients,
      systemComputationParticipantsClient = systemComputationParticipantsClient,
      systemComputationsClient = systemComputationsClient,
      systemComputationLogEntriesClient = systemComputationLogEntriesClient,
      computationStatsClient = computationStatsClient,
      computationType = ComputationType.UNSPECIFIED,
      workLockDuration = java.time.Duration.ofSeconds(1),
      requestChunkSizeBytes = 1,
      maximumAttempts = 1,
      clock = java.time.Clock.systemUTC(),
      rpcRetryBackoff =
        ExponentialBackoff(
          initialDelay = java.time.Duration.ofMillis(1),
          randomnessFactor = 0.0,
        ),
      rpcMaxAttempts = 2,
    ) {
    override val endingStage: ComputationStage = ComputationStage.getDefaultInstance()

    override suspend fun processComputationImpl(token: ComputationToken) = Unit

    suspend fun touchUpdateComputationParticipant(token: ComputationToken) {
      updateComputationParticipant(token) { throw Status.UNAVAILABLE.asException() }
    }
  }

  private fun initMcSimulator() {
    val measurementConsumerData = inProcessCmmsComponents.getMeasurementConsumerData()
    mcSimulator =
      EdpAggregatorMeasurementConsumerSimulator(
        MeasurementConsumerData(
          measurementConsumerData.name,
          InProcessCmmsComponents.MC_ENTITY_CONTENT.signingKey,
          InProcessCmmsComponents.MC_ENCRYPTION_PRIVATE_KEY,
          measurementConsumerData.apiAuthenticationKey,
        ),
        OUTPUT_DP_PARAMS,
        publicDataProvidersClient,
        publicEventGroupsClient,
        publicMeasurementsClient,
        publicMeasurementConsumersClient,
        publicCertificatesClient,
        InProcessCmmsComponents.TRUSTED_CERTIFICATES,
        TestEvent.getDefaultInstance(),
        NoiseMechanism.CONTINUOUS_GAUSSIAN,
        syntheticPopulationSpec,
        syntheticEventGroupMap,
        ReportKey(
            MeasurementConsumerKey.fromName(measurementConsumerData.name)!!.measurementConsumerId,
            "some-report-id",
          )
          .toName(),
        modelLineName = modelLineName,
        onMeasurementsCreated = {
          if (!dataWatcherStarted) {
            inProcessEdpAggregatorComponents.startDataWatcher()
            dataWatcherStarted = true
          }
          if (resultsFulfillerJob == null) {
            resultsFulfillerJob = inProcessEdpAggregatorComponents.startResultsFulfiller()
          }
          if (requisitionFetcherJob == null) {
            requisitionFetcherJob =
              inProcessEdpAggregatorComponents.startRequisitionFetchers(
                iterations = 300,
                interval = java.time.Duration.ofSeconds(1),
              )
          }
        },
      )
  }

  @After
  fun tearDown() {
    inProcessCmmsComponents.stopDuchyDaemons()
    inProcessCmmsComponents.stopPopulationRequisitionFulfillerDaemon()
    inProcessEdpAggregatorComponents.stopDaemons()
    runBlocking {
      pubSubClient.deleteTopic(PROJECT_ID, FULFILLER_TOPIC_ID)
      pubSubClient.deleteSubscription(PROJECT_ID, SUBSCRIPTION_ID)
    }
  }

  @Test
  fun `create a direct RF measurement and check the result is equal to the expected result`() =
    runBlocking {
      // Use frontend simulator to create a direct reach and frequency measurement and verify its
      // result.
      mcSimulator.testDirectReachAndFrequency(runId = "1234", numMeasurements = 1)
    }

  @Test
  fun `create a direct reach only measurement and check the result is equal to the expected result`() =
    runBlocking {
      // Use frontend simulator to create a direct reach and frequency measurement and verify its
      // result.
      mcSimulator.testDirectReachOnly(runId = "1234", numMeasurements = 1)
    }

  @Test
  fun `create incremental direct reach only measurements in same report and check the result is equal to the expected result`() =
    runBlocking {
      // Use frontend simulator to create N incremental direct reach and frequency measurements and
      // verify its result.
      mcSimulator.testDirectReachOnly(runId = "1234", numMeasurements = 3)
    }

  @Test
  fun `create an impression measurement and check the result is equal to the expected result`() =
    runBlocking {
      // Use frontend simulator to create an impression measurement and verify its result.
      mcSimulator.testImpression("1234")
    }

  @Test
  fun `create a Hmss reach-only measurement and check the result is equal to the expected result`() =
    runBlocking {
      // Use frontend simulator to create a reach and frequency measurement and verify its result.
      mcSimulator.testReachOnly(
        "1234",
        DataProviderKt.capabilities { honestMajorityShareShuffleSupported = true },
      )
    }

  @Test
  fun `create a Hmss RF measurement and check the result is equal to the expected result`() =
    runBlocking {
      // Use frontend simulator to create a reach and frequency measurement and verify its result.
      mcSimulator.testReachAndFrequency(
        "1234",
        DataProviderKt.capabilities { honestMajorityShareShuffleSupported = true },
      )
    }

  companion object {
    private val logger: Logger = Logger.getLogger(this::class.java.name)
    private var coverageTouchesDone = false
    private var coverageComputationIdCounter = 0
    private val modelLineName =
      "modelProviders/AAAAAAAAAHs/modelSuites/AAAAAAAAAHs/modelLines/AAAAAAAAAHs"
    // Epsilon can vary from 0.0001 to 1.0, delta = 1e-15 is a realistic value.
    // Set epsilon higher without exceeding privacy budget so the noise is smaller in the
    // integration test. Check sample values in CompositionTest.kt.
    private val OUTPUT_DP_PARAMS = differentialPrivacyParams {
      epsilon = 1.0
      delta = 1e-15
    }

    // This is the relative location from which population and data spec textprotos are read.
    private val TEST_DATA_PATH =
      Paths.get(
        "wfa_measurement_system",
        "src",
        "main",
        "proto",
        "wfa",
        "measurement",
        "loadtest",
        "dataprovider",
      )
    private val TEST_DATA_RUNTIME_PATH = getRuntimePath(TEST_DATA_PATH)!!

    private val TEST_RESULTS_FULFILER_DATA_PATH =
      Paths.get(
        "wfa_measurement_system",
        "src",
        "main",
        "kotlin",
        "org",
        "wfanet",
        "measurement",
        "edpaggregator",
        "resultsfulfiller",
        "testing",
      )
    private val TEST_RESULTS_FULFILLER_DATA_RUNTIME_PATH =
      getRuntimePath(TEST_RESULTS_FULFILER_DATA_PATH)!!

    val syntheticPopulationSpec: SyntheticPopulationSpec =
      parseTextProto(
        TEST_DATA_RUNTIME_PATH.resolve("small_population_spec.textproto").toFile(),
        SyntheticPopulationSpec.getDefaultInstance(),
      )
    val syntheticEventGroupSpec: SyntheticEventGroupSpec =
      parseTextProto(
        TEST_DATA_RUNTIME_PATH.resolve("small_data_spec.textproto").toFile(),
        SyntheticEventGroupSpec.getDefaultInstance(),
      )
    val populationSpec =
      parseTextProto(
        TEST_RESULTS_FULFILLER_DATA_RUNTIME_PATH.resolve("small_population_spec.textproto")
          .toFile(),
        PopulationSpec.getDefaultInstance(),
      )
    val modelLineInfoMap =
      mapOf(
        modelLineName to
          ModelLineInfo(
            populationSpec = populationSpec,
            vidIndexMap = InMemoryVidIndexMap.build(populationSpec),
            eventDescriptor = TestEvent.getDescriptor(),
          )
      )

    @BeforeClass
    @JvmStatic
    fun initConfig() {
      InProcessCmmsComponents.initConfig()
    }

    @get:ClassRule @JvmStatic val pubSubEmulatorProvider = GooglePubSubEmulatorProvider()
  }
}
