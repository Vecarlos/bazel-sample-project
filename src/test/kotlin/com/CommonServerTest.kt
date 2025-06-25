package com

import com.GrpcServer
import com.ServerConfiguration
import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class CommonServerTest {

    @Mock private lateinit var mockInternalGrpcServer: Server

    @Mock private lateinit var mockServerBuilderInstance: ServerBuilder<*>

    @Mock private lateinit var mockBindableService: BindableService

    private val fakePort = 8080
    private val portServerConfiguration = ServerConfiguration(fakePort)

    private var mockEstaticoServerBuilder: MockedStatic<ServerBuilder<*>>? = null

    @Before
    fun setUp() {
        mockEstaticoServerBuilder?.close()
        mockEstaticoServerBuilder = Mockito.mockStatic(ServerBuilder::class.java)

        mockEstaticoServerBuilder
                ?.`when`<ServerBuilder<*>> { ServerBuilder.forPort(any()) }
                ?.thenReturn(mockServerBuilderInstance)
        mockEstaticoServerBuilder
                ?.`when`<ServerBuilder<*>> { ServerBuilder.forPort(fakePort) }
                ?.thenReturn(mockServerBuilderInstance)

        whenever(mockServerBuilderInstance.addService(any<BindableService>()))
                .thenReturn(mockServerBuilderInstance)
        whenever(mockServerBuilderInstance.build()).thenReturn(mockInternalGrpcServer)
    }

    @After
    fun tearDown() {
        mockEstaticoServerBuilder?.close()
        mockEstaticoServerBuilder = null
    }

    @Test
    fun `create must configurate and build a server`() {
        val servidorGrpc = GrpcServer.create(portServerConfiguration, mockBindableService)

        mockEstaticoServerBuilder?.verify { ServerBuilder.forPort(fakePort) }
        Mockito.verify(mockServerBuilderInstance).addService(mockBindableService)
        Mockito.verify(mockServerBuilderInstance).build()

        servidorGrpc.start()
        Mockito.verify(mockInternalGrpcServer).start()

        servidorGrpc.shutdown()
        Mockito.verify(mockInternalGrpcServer).shutdown()
    }

    @Test
    fun `Start must call start in the grpc server`() {
        val servidorGrpc = GrpcServer.create(portServerConfiguration, mockBindableService)
        servidorGrpc.start()

        Mockito.verify(mockInternalGrpcServer).start()
    }

    @Test
    fun `Shutdown must call shutdown in the grpc server`() {
        val servidorGrpc = GrpcServer.create(portServerConfiguration, mockBindableService)
        servidorGrpc.shutdown()

        Mockito.verify(mockInternalGrpcServer).shutdown()
    }

    @Test
    fun `waitUntilShutdown must call awaitTermination in the grpc server`() = runBlocking {
        doNothing().whenever(mockInternalGrpcServer).awaitTermination()

        val servidorGrpc = GrpcServer.create(portServerConfiguration, mockBindableService)
        servidorGrpc.waitUntilShutdown()

        Mockito.verify(mockInternalGrpcServer).awaitTermination()
    }
}
