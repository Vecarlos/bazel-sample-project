package sample

// SumEvaluator.kt
class SumEvaluator {
  fun evaluate(a: Int, b: Int): Int{
    if (a > 100){
      return a+a
    }
    return a + b
  }
}