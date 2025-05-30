package client
// reference https://github.com/world-federation-of-advertisers/cross-media-measurement/blob/d2d8b14b8d3c08b26938f3721849e10e8056a9a1/src/main/kotlin/org/wfanet/measurement/duchy/deploy/common/job/mill/shareshuffle/HonestMajorityShareShuffleMillJob.kt#L42
import kotlinx.coroutines.runBlocking
import picocli.CommandLine
import kotlin.system.exitProcess
import client.Flags.CountLettersClientFlags

@CommandLine.Command(
    name = "CountLettersClientApp",
    description = ["gRPC client for CountLettersService"],
    mixinStandardHelpOptions = true,
    showDefaultValues = true
)
class CountLettersClientApp : Runnable {
    @CommandLine.Mixin
    lateinit var flags: CountLettersClientFlags
        private set

    override fun run() {
        val client = CountLettersClient(flags.host, flags.port)
        runBlocking {
        client.run(flags.textToCount)
        }
    }
}

fun main(args: Array<String>) {
    val exitCode = CommandLine(CountLettersClientApp()).execute(*args)
    exitProcess(exitCode)
}
