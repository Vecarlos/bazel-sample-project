package com
// reference
// https://github.com/world-federation-of-advertisers/common-jvm/blob/main/src/main/kotlin/org/wfanet/measurement/common/grpc/CommonServer.kt

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

data class ServerConfiguration(val port: Int)

class GrpcServer private constructor(private val serverGrpc: Server) {

    fun start() {
        serverGrpc.start()
        println("Server started in port ${serverGrpc.port}")

        Runtime.getRuntime()
                .addShutdownHook(
                        Thread {
                            System.err.println("*** GrpcServer: shutting down...")
                            this.shutdown()
                        }
                )
    }

    fun shutdown() {
        serverGrpc.shutdown()
    }

    suspend fun waitUntilShutdown() {
        runInterruptible(Dispatchers.IO) { serverGrpc.awaitTermination() }
    }

    companion object {
        fun create(config: ServerConfiguration, servicio: BindableService): GrpcServer {

            val builder = ServerBuilder.forPort(config.port)
            builder.addService(servicio)
            return GrpcServer(builder.build())
        }
    }
}
