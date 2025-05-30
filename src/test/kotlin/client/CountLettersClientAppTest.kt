package client

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedConstruction
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import picocli.CommandLine
import client.Flags.CountLettersClientFlags

@RunWith(MockitoJUnitRunner::class)
class CountLettersClientAppTest {

    @Test
    fun `run should instantiate Client with command line flags and call its run method`() = runBlocking {
        val testHost = "my-custom-host"
        val testPort = 12345
        val testText = "test input string"

        val app = CountLettersClientApp()

        val mockConstruction: MockedConstruction<CountLettersClient> =
            Mockito.mockConstruction(CountLettersClient::class.java) { mock, context ->
                assertEquals(testHost, context.arguments()[0])
                assertEquals(testPort, context.arguments()[1])
            }

        CommandLine(app).execute(
            "--host", testHost,
            "--port", testPort.toString(),
            "--text", testText
        )

        val constructedClients = mockConstruction.constructed()
        assertEquals(1, constructedClients.size)
        val mockedClient = constructedClients[0]

        verify(mockedClient).run(testText)

        mockConstruction.close()
    }

    @Test
    fun `run should use default host and port if not provided`() = runBlocking {
        val defaultFlags = CountLettersClientFlags()
        val testText = "text with default host port"
        val app = CountLettersClientApp()

        val mockConstruction = Mockito.mockConstruction(CountLettersClient::class.java) { mock, context ->
            assertEquals(defaultFlags.host, context.arguments()[0])
            assertEquals(defaultFlags.port, context.arguments()[1])
        }


        CommandLine(app).execute("--text", testText)

        val constructedClients = mockConstruction.constructed()
        assertEquals(1, constructedClients.size)
        val mockedClient = constructedClients[0]

        verify(mockedClient).run(testText)

        mockConstruction.close()

    }
}