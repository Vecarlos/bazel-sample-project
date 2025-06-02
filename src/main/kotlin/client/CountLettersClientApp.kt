package client
// reference https://github.com/world-federation-of-advertisers/cross-media-measurement/blob/d2d8b14b8d3c08b26938f3721849e10e8056a9a1/src/main/kotlin/org/wfanet/measurement/duchy/deploy/common/job/mill/shareshuffle/HonestMajorityShareShuffleMillJob.kt#L42
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val host: String
    val port: Int
    val textToCount: String

    if (args.size == 3){
        host = args[0]
        port = args[1].toIntOrNull() ?: run {
            println("ERROR: Port must be a valid number")
            return
        }
        textToCount = args[2]
    } else {
        host = "localhost"
        port = 50051
        textToCount = args[0]
    }
    val client = CountLettersClient(host, port)
    runBlocking {
        client.run(textToCount)
    }
}