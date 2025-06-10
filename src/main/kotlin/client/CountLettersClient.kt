package client
// references
// https://github.com/world-federation-of-advertisers/cross-media-measurement/blob/main/src/main/kotlin/org/wfanet/panelmatch/client/launcher/GrpcApiClient.kt
// https://github.com/world-federation-of-advertisers/common-jvm/blob/main/src/main/kotlin/org/wfanet/measurement/common/grpc/Channel.kt
import io.grpc.ManagedChannel
import io.grpc.netty.NettyChannelBuilder
import service.proto.CountLettersServiceGrpcKt
import service.proto.InputString

class CountLettersClient(private val host: String, private val port: Int) {
    suspend fun run(input: String) {
        val target = "$host:$port"
        val channel: ManagedChannel = NettyChannelBuilder.forTarget(target).usePlaintext().build()

        val stub = CountLettersServiceGrpcKt.CountLettersServiceCoroutineStub(channel)

        val request = InputString.newBuilder().setInputString(input).build()
        val response = stub.countLetters(request)

        println("The number of letters is: ${response.letterNumber}")

        channel.shutdown()
    }
}
