package com.Flags
// reference https://github.com/world-federation-of-advertisers/common-jvm/blob/main/src/main/kotlin/org/wfanet/measurement/common/grpc/TlsFlags.kt
import picocli.CommandLine

class CountLettersServerFlags {
    @CommandLine.Option(names = ["--port", "-p"], description = ["GRPC server port."], defaultValue = "50051")
    var port: Int = 50051
        private set
}