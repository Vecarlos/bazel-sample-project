package sample

import com.google.crypto.tink.integration.awskms.AwsKmsClient
import java.util.Optional

fun main() {
    AwsKmsClient.register(Optional.empty(), Optional.empty())
}