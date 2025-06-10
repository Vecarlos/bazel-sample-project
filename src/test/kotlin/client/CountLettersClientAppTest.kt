package client

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedConstruction
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class CountLettersClientAppTest {

    @Test
    fun `main with 3 valid arguments should create client and run`() {
        val testHost = "testhost.com"
        val testPort = 12345
        val testText = "three_arguments_test"

        var mockConstruction: MockedConstruction<CountLettersClient>? = null
        try {
            mockConstruction =
                    Mockito.mockConstruction(CountLettersClient::class.java) { mock, context ->
                        assertEquals(testHost, context.arguments()[0])
                        assertEquals(testPort, context.arguments()[1])
                        runBlocking { whenever(mock.run(any())).thenReturn(Unit) }
                    }

            main(arrayOf(testHost, testPort.toString(), testText))

            val constructedClients = mockConstruction.constructed()
            assertEquals(1, constructedClients.size)
            val mockedClient = constructedClients[0]
            runBlocking { verify(mockedClient).run(testText) }
        } finally {
            mockConstruction?.close()
        }
    }

    @Test
    fun `main with 1 argument should use default host-port and run`() {
        val testText = "one_argument_test"

        var mockConstruction: MockedConstruction<CountLettersClient>? = null
        try {
            mockConstruction =
                    Mockito.mockConstruction(CountLettersClient::class.java) { mock, context ->
                        assertEquals("localhost", context.arguments()[0])
                        assertEquals(50051, context.arguments()[1])
                        runBlocking { whenever(mock.run(any())).thenReturn(Unit) }
                    }

            main(arrayOf(testText))

            val constructedClients = mockConstruction.constructed()
            assertEquals(1, constructedClients.size)
            val mockedClient = constructedClients[0]
            runBlocking { verify(mockedClient).run(testText) }
        } finally {
            mockConstruction?.close()
        }
    }

    @Test
    fun `main with 2 arguments should use default host-port and arg0 as text`() {
        val arg0Text = "text_for_arg0"
        val arg1Ignored = "this_is_ignored"

        var mockConstruction: MockedConstruction<CountLettersClient>? = null
        try {
            mockConstruction =
                    Mockito.mockConstruction(CountLettersClient::class.java) { mock, context ->
                        assertEquals("localhost", context.arguments()[0])
                        assertEquals(50051, context.arguments()[1])
                        runBlocking { whenever(mock.run(any())).thenReturn(Unit) }
                    }

            main(arrayOf(arg0Text, arg1Ignored))

            val constructedClients = mockConstruction.constructed()
            assertEquals(1, constructedClients.size)
            val mockedClient = constructedClients[0]
            runBlocking { verify(mockedClient).run(arg0Text) }
        } finally {
            mockConstruction?.close()
        }
    }

    @Test
    fun `main with invalid port argument should print error and not create client`() {
        val originalSystemOut = System.out
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))

        var mockConstruction: MockedConstruction<CountLettersClient>? = null
        try {
            mockConstruction =
                    Mockito.mockConstruction(CountLettersClient::class.java) { mock, context -> }

            main(arrayOf("somehost", "not-a-number", "sometext"))

            val consoleOutput = outputStreamCaptor.toString().trim()
            assertTrue(consoleOutput.contains("ERROR: Port must be a valid number"))

            assertEquals(0, mockConstruction.constructed().size)
        } finally {
            mockConstruction?.close()
            System.setOut(originalSystemOut) // Restaura System.out
        }
    }
}
