package client

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import service.proto.CountLettersServiceGrpcKt
import service.proto.InputString
import service.proto.LetterNumber

@RunWith(MockitoJUnitRunner::class)
class CountLettersClientTest {

    @Mock
    private lateinit var mockChannel: ManagedChannel

    @Mock
    private lateinit var mockChannelBuilderInstance: ManagedChannelBuilder<*>

    @Mock
    private lateinit var mockProtoResponse: LetterNumber

    private val testHost = "test-host"
    private val testPort = 12345
    private lateinit var client: CountLettersClient

    private var staticManagedChannelBuilderMockContext: MockedStatic<ManagedChannelBuilder<*>>? = null
    private var stubConstructorMockContext: MockedConstruction<CountLettersServiceGrpcKt.CountLettersServiceCoroutineStub>? = null

    @Before
    fun setUp() {
        client = CountLettersClient(testHost, testPort)

        staticManagedChannelBuilderMockContext = Mockito.mockStatic(ManagedChannelBuilder::class.java)
        staticManagedChannelBuilderMockContext?.`when`<ManagedChannelBuilder<*>> {
            ManagedChannelBuilder.forAddress(testHost, testPort)
        }?.thenReturn(mockChannelBuilderInstance)

        whenever(mockChannelBuilderInstance.usePlaintext()).thenReturn(mockChannelBuilderInstance)
        whenever(mockChannelBuilderInstance.build()).thenReturn(mockChannel)

        stubConstructorMockContext = Mockito.mockConstruction(
            CountLettersServiceGrpcKt.CountLettersServiceCoroutineStub::class.java
        ) { mock, context ->
            assertEquals(mockChannel, context.arguments()[0])
            runBlocking {
                whenever(mock.countLetters(any<InputString>(), any<Metadata>())).thenReturn(mockProtoResponse)
            }
        }
        whenever(mockProtoResponse.letterNumber).thenReturn(99)
    }

    @After
    fun tearDown() {
        staticManagedChannelBuilderMockContext?.close()
        stubConstructorMockContext?.close()
    }

    @Test
    fun `run should correctly call grpc service and shutdown channel`() {
        runBlocking {
            val testInput = "hello world"
            client.run(testInput)

            staticManagedChannelBuilderMockContext!!.verify {
                ManagedChannelBuilder.forAddress(testHost, testPort)
            }
            verify(mockChannelBuilderInstance).usePlaintext()
            verify(mockChannelBuilderInstance).build()

            val constructedStubs = stubConstructorMockContext!!.constructed()
            assertTrue(constructedStubs.isNotEmpty())
            val usedStub = constructedStubs[0]

            val requestCaptor = argumentCaptor<InputString>()
            verify(usedStub).countLetters(requestCaptor.capture(), any<Metadata>())
            assertEquals(testInput, requestCaptor.firstValue.inputString)

            verify(mockChannel).shutdown()
        }
    }
}