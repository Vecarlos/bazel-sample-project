package com.CountLettersServer
import com.CommonServer.GrpcServer
import com.CommonServer.ServerConfiguration
import com.Flags.CountLettersServerFlags
// reference https://github.com/world-federation-of-advertisers/cross-media-measurement/blob/3779f3f887278e2d46c72f348dc55fe931e71ca4/src/main/kotlin/org/wfanet/measurement/duchy/deploy/common/server/ComputationControlServer.kt

import implementation.CountLettersServiceImpl
import kotlinx.coroutines.runBlocking
import picocli.CommandLine


abstract class AbstractCountLettersLauncher : Runnable {
    @CommandLine.Mixin
    internal lateinit var serverFlags: CountLettersServerFlags
        private set
    protected fun runLauncher() {
        val config = ServerConfiguration(port = serverFlags.port)
        val serviceImpl = CountLettersServiceImpl()

        GrpcServer.create(
            config,
            serviceImpl
        )
        .start() 


        runBlocking { 
            GrpcServer.create(config, serviceImpl).waitUntilShutdown()
        }
    }

    protected fun runLauncherClean() = runBlocking {
        val config = ServerConfiguration(port = serverFlags.port)
        val serviceImpl = CountLettersServiceImpl()
        val serverInstance = GrpcServer.create(config, serviceImpl)

        serverInstance.start()
        serverInstance.waitUntilShutdown()
    }
}

@CommandLine.Command(
    name = "CountLettersServerApp",
    description = ["CountLetters server using GRPCserver"],
    mixinStandardHelpOptions = true,
    showDefaultValues = true
)
class CountLettersServerApp : AbstractCountLettersLauncher() {
    val parsedFlags: CountLettersServerFlags
        get() = serverFlags

    override fun run() {
        runLauncherClean()
    }
}

fun main(args: Array<String>) {
    val exitCode = picocli.CommandLine(CountLettersServerApp()).execute(*args)

}