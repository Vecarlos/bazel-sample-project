package com

import com.Flags.CountLettersServerFlags
import org.junit.Assert.assertEquals
import org.junit.Test
import picocli.CommandLine

class FlagsTest {

    @Test
    fun `should use default port when no port argument is provided`() {
        val flags = CountLettersServerFlags()
        CommandLine(flags).parseArgs() 
        assertEquals(50051, flags.port)
    }

    @Test
    fun `should parse port argument when --port is provided`() {
        val flags = CountLettersServerFlags()
        val testPort = 12345
        CommandLine(flags).parseArgs("--port", testPort.toString())
        assertEquals(testPort, flags.port)
    }

}