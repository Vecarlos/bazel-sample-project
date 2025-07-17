
package replicate_error

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertEquals
import replicate_error.SumEvaluator


@RunWith(JUnit4::class)
class FunTest {
  private val evaluator = SumEvaluator()

  @Test
  fun `when a = 1 and b = 2, then returns  3`() {
    val result = evaluator.evaluate(1, 2)
    assertEquals(3, result)
  }
}