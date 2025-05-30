package client 

import io.grpc.ManagedChannel
import io.grpc.Server
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

// Importaciones de las clases generadas por tu .proto (basado en java_package = "service.proto")
import service.proto.CountLettersServiceGrpcKt.CountLettersServiceCoroutineStub
import service.proto.InputString
import service.proto.LetterNumber
// No necesitas importar CountLettersServiceCoroutineImplBase aquí para el test del cliente

@RunWith(JUnit4::class)
class CountLettersServiceImplIntegrationTest {

    @get:Rule
    val grpcCleanup: GrpcCleanupRule = GrpcCleanupRule()

    @Test
    fun `client should receive correct character count from in-process server`() {
        runBlocking {
            val serviceImpl = CountLettersServiceImpl() // Tu implementación de servicio

            val serverName = InProcessServerBuilder.generateName()

            val server: Server = InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(serviceImpl) // Añade tu implementación
                .build()
                .start()
            grpcCleanup.register(server)

            val channel: ManagedChannel = InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build()
            grpcCleanup.register(channel)

            val clientStub = CountLettersServiceCoroutineStub(channel) // Usa el stub generado

            val testText = "Test123!"
            // Usa el mensaje de petición generado
            val request = InputString.newBuilder().setInputString(testText).build()

            val response: LetterNumber = clientStub.countLetters(request) // Llama al método RPC

            // Verifica la respuesta basada en la lógica de TU CountLettersServiceImpl
            // que usa inputText.length
            assertEquals(testText.length, response.letterNumber)
        }
    }

    @Test
    fun `client with empty string should receive zero from in-process server`() {
        runBlocking {
            val serviceImpl = CountLettersServiceImpl()
            val serverName = InProcessServerBuilder.generateName()

            val server: Server = InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(serviceImpl)
                .build()
                .start()
            grpcCleanup.register(server)

            val channel: ManagedChannel = InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build()
            grpcCleanup.register(channel)
            
            val clientStub = CountLettersServiceCoroutineStub(channel)

            val testText = ""
            val request = InputString.newBuilder().setInputString(testText).build()
            val response: LetterNumber = clientStub.countLetters(request)

            assertEquals(0, response.letterNumber)
        }
    }
}