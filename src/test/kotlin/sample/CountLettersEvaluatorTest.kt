package sample

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CountLettersEvaluatorTest {

  private val evaluator = CountLettersEvaluator()

  @Test
  fun `when string is "hello", then returns  5`() {
    val result = evaluator.evaluate("hello")
    assertEquals(5, result)
  }

  @Test
  fun `when string is empty, then returns  0`() {
    val result = evaluator.evaluate("")
    assertEquals(0, result)
  }
}
