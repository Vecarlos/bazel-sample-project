package com.CommonServer

import io.grpc.BindableService 
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

data class ServerConfiguration(val port: Int)

class GrpcServer private constructor(private val servidorGrpc: Server) {

    fun start() {
        servidorGrpc.start()
        println("Server started in port ${servidorGrpc.port}")

        Runtime.getRuntime().addShutdownHook(Thread {
            System.err.println("*** GrpcServer: shutting down...")
            this.shutdown()
        })
    }

    fun shutdown() {
        servidorGrpc.shutdown()
    }

    suspend fun waitUntilShutdown() {
        runInterruptible(Dispatchers.IO) { 
            servidorGrpc.awaitTermination()
        }
    }

    companion object {
        fun crear(
            config: ServerConfiguration,
            servicio: BindableService
        ): GrpcServer {

            val builder = ServerBuilder.forPort(config.port)
            builder.addService(servicio)
            return GrpcServer(builder.build())
        }
    }
}