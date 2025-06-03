package com.CountLettersServer
// reference
// https://github.com/world-federation-of-advertisers/cross-media-measurement/blob/3779f3f887278e2d46c72f348dc55fe931e71ca4/src/main/kotlin/org/wfanet/measurement/duchy/deploy/common/server/ComputationControlServer.kt
import com.CommonServer.GrpcServer
import com.CommonServer.ServerConfiguration
import implementation.CountLettersServiceImpl
import kotlinx.coroutines.runBlocking

class CountLettersServer(private val portToUse: Int) : Runnable {
    override fun run() {
        runBlocking {
            val config = ServerConfiguration(port = portToUse)
            val serviceImpl = CountLettersServiceImpl()
            val serverInstance = GrpcServer.create(config, serviceImpl)
            serverInstance.start()
            serverInstance.waitUntilShutdown()
        }
    }
}

fun main(args: Array<String>) {
    var port = 50051

    if (args.isNotEmpty()) {
        val parsedPort = args[0].toIntOrNull()
        if (parsedPort != null) {
            port = parsedPort
        }
    }

    val app = CountLettersServer(portToUse = port)
    app.run()
}
