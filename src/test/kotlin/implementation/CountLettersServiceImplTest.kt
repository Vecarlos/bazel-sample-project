package implementation // O el paquete donde quieras poner tu test

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertEquals
import service.proto.InputString
import service.proto.LetterNumber 

@RunWith(JUnit4::class)
class CountLettersServiceImplTest {

  private val serviceImpl = CountLettersServiceImpl()

  @Test
  fun `countLetters should return correct letter count`() = runBlocking {
    // Prepara la entrada
    val testString = "Hola123Mundo!"
    val request = InputString.newBuilder().setInputString(testString).build()

    // Llama al método del servicio
    val response = serviceImpl.countLetters(request)

    // Verifica la salida
    // "HolaMundo" tiene 9 letras
    val expectedLetterCount = 9
    assertEquals(expectedLetterCount, response.letterNumber)
  }

  @Test
  fun `countLetters with no letters should return zero`() = runBlocking {
    val testString = "123 !@#"
    val request = InputString.newBuilder().setInputString(testString).build()

    val response = serviceImpl.countLetters(request)

    assertEquals(0, response.letterNumber)
  }

  @Test
  fun `countLetters with empty string should return zero`() = runBlocking {
    val testString = ""
    val request = InputString.newBuilder().setInputString(testString).build()

    val response = serviceImpl.countLetters(request)

    assertEquals(0, response.letterNumber)
  }
}