package com

import org.junit.Test
import kotlin.test.assertNotNull

class CommonServerKtTest {
    @Test
    fun testStart() {
        val server = CommonServerKt()
        server.start()
        assertNotNull(server)
    }
    
    @Test
    fun testStop() {
        val server = CommonServerKt()
        server.stop()
        assertNotNull(server)
    }
}