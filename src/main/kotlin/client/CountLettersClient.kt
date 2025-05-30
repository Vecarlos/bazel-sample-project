package client
//reference https://github.com/world-federation-of-advertisers/cross-media-measurement/blob/main/src/main/kotlin/org/wfanet/panelmatch/client/launcher/GrpcApiClient.kt
import io.grpc.ManagedChannelBuilder
import service.proto.CountLettersServiceGrpcKt
import service.proto.InputString

class CountLettersClient(private val host: String, private val port: Int) {
    suspend fun run(input: String) {
        val channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build()

        val stub = CountLettersServiceGrpcKt.CountLettersServiceCoroutineStub(channel)

        val request = InputString.newBuilder().setInputString(input).build()
        val response = stub.countLetters(request)

        println("The number of letters is: ${response.letterNumber}")

        channel.shutdown()
    }
}
