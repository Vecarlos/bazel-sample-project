package implementation // O el paquete donde quieras poner tu test

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import service.proto.InputString

@RunWith(JUnit4::class)
class CountLettersServiceImplTest {

  private val serviceImpl = CountLettersServiceImpl()

  @Test
  fun `countLetters should return correct letter count`() = runBlocking {
    val testString = "Hello world"
    val request = InputString.newBuilder().setInputString(testString).build()
    val response = serviceImpl.countLetters(request)
    val expectedLetterCount = 11
    assertEquals(expectedLetterCount, response.letterNumber)
  }

  @Test
  fun `countLetters with empty string should return zero`() = runBlocking {
    val testString = ""
    val request = InputString.newBuilder().setInputString(testString).build()

    val response = serviceImpl.countLetters(request)

    assertEquals(0, response.letterNumber)
  }
}
