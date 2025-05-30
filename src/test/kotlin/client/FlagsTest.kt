package client

import client.Flags.CountLettersClientFlags
import org.junit.Assert.assertEquals
import org.junit.Test
import picocli.CommandLine

class FlagsTest {

    @Test
    fun `should use default port when no port argument is provided`() {
        val flags = CountLettersClientFlags()
        CommandLine(flags).parseArgs("--text", "dummyText") 
        assertEquals(50051, flags.port)
    }

    @Test
    fun `should parse port argument when --port is provided`() {
        val flags = CountLettersClientFlags()
        val testPort = 12345
        CommandLine(flags).parseArgs("--port", testPort.toString(), "--text", "dummyText")
        assertEquals(testPort, flags.port)
    }

    @Test
    fun `should parse host argument when --host is provided`() {
        val flags = CountLettersClientFlags()
        val testHost = "localhost"
        CommandLine(flags).parseArgs("--host", testHost, "--text", "dummyText")
        assertEquals(testHost, flags.host)
    }


    @Test
    fun `should parse text argument when --text is provided`() {
        val flags = CountLettersClientFlags()
        val testText = "text"
        CommandLine(flags).parseArgs("--text", testText)
        assertEquals(testText, flags.textToCount)
    }

}