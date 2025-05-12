import unittest
from sample.CountLettersEvaluator import CountLettersEvaluator


class TestCountLettersEvaluator(unittest.TestCase):

    def test_evaluate_hello(self):
        self.assertEqual(CountLettersEvaluator.evaluate(
            "hello"), 5, "when string is 'hello', then returns  5")

    def test_evaluate_empty(self):
        self.assertEqual(CountLettersEvaluator.evaluate(
            ""), 0, "when string is empty, then returns  0")


if __name__ == '__main__':
    unittest.main()
