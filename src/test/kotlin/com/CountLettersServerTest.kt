package com

import com.GrpcServer
import com.CountLettersServer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class CountLettersServerTest {

    @Mock private lateinit var mockedGrpcServerInstance: GrpcServer

    @Test
    fun `main should create CountLettersServer with correct port from args`() {
        val testPort = 8080
        val args = arrayOf(testPort.toString())

        Mockito.mockConstruction(CountLettersServer::class.java).use { serverMock ->
            main(args)

            assertEquals(1, serverMock.constructed().size)
            val constructedServer = serverMock.constructed()[0]

            verify(constructedServer).run()
        }
    }

    @Test
    fun `main should use default port when no args provided`() {
        val args = arrayOf<String>()

        Mockito.mockConstruction(CountLettersServer::class.java).use { serverMock ->
            main(args)

            assertEquals(1, serverMock.constructed().size)
            val constructedServer = serverMock.constructed()[0]

            verify(constructedServer).run()
        }
    }
}
