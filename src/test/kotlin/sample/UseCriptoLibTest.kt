package sample

import com.google.crypto.tink.integration.awskms.AwsKmsClient
import org.junit.Test
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import java.util.Optional
import org.junit.runners.JUnit4
import org.junit.runner.RunWith


@RunWith(JUnit4::class)
class UseCriptoLibTest {

    @Test
    fun `main should register AwsKmsClient`() {
        val awsKmsClientMock = mockStatic(AwsKmsClient::class.java)

        awsKmsClientMock.use {
            main()
            it.verify { AwsKmsClient.register(Optional.empty(), Optional.empty()) }
        }
    }
}