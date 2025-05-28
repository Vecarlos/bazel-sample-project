package com

import com.Flags.CountLettersServerFlags
import com.CountLettersServer.CountLettersServerApp
import org.junit.Assert.assertEquals
import org.junit.Test
import picocli.CommandLine

class CountLettersServerTest {

    @Test
    fun `should parse port argument correctly`() {
        val testPort = 12345
        val app = CountLettersServerApp()
        
        val commandLine = CommandLine(app)
        commandLine.parseArgs("--port", testPort.toString())
        
        assertEquals(testPort, app.parsedFlags.port)
    }

    @Test  
    fun `should use default port when no arguments provided`() {
        val app = CountLettersServerApp()
        val expectedDefaultPort = CountLettersServerFlags().port
        
        val commandLine = CommandLine(app)
        commandLine.parseArgs() 
        
        assertEquals(expectedDefaultPort, app.parsedFlags.port)
    }

    @Test
    fun `should parse multiple arguments correctly`() {
        val testPort = 9090
        val app = CountLettersServerApp()
        
        val commandLine = CommandLine(app)
        commandLine.parseArgs("--port", testPort.toString())
        
        assertEquals(testPort, app.parsedFlags.port)
    }

    @Test
    fun `flags should have sensible default values`() {
        val flags = CountLettersServerFlags()
        
        assertEquals(50051, flags.port) 
        assert(flags.port > 0) 
        assert(flags.port < 65536)
    }

    @Test
    fun `should handle help flag without issues`() {
        val app = CountLettersServerApp()
        val commandLine = CommandLine(app)
        commandLine.execute("--help")

    }
}