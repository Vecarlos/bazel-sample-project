package client.Flags
//reference https://github.com/world-federation-of-advertisers/cross-media-measurement/blob/a8d32981e8d085caa3de7795379e0fcdb9c5a8a5/src/main/kotlin/org/wfanet/panelmatch/client/deploy/ExchangeWorkflowFlags.kt#L27
import picocli.CommandLine

class CountLettersClientFlags {
    @CommandLine.Option(names = ["--host"], description = ["gRPC server hostname"], defaultValue = "localhost")
    var host: String = "localhost"
        private set

    @CommandLine.Option(names = ["--port"], description = ["gRPC server port"], defaultValue = "50051")
    var port: Int = 50051
        private set
    
    @CommandLine.Option(
        names = ["--text"],
        description = ["The string to count letters from."],
        required = true 
    )
    lateinit var textToCount: String
        private set
}
